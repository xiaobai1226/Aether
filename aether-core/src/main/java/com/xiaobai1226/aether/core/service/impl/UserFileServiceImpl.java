package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.xiaobai1226.aether.common.constant.SystemConsts;
import com.xiaobai1226.aether.common.enums.CategoryEnum;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.enums.FileTypeEnum;
import com.xiaobai1226.aether.core.cache.FileCache;
import com.xiaobai1226.aether.core.cache.UserCache;
import com.xiaobai1226.aether.core.domain.dto.*;
import com.xiaobai1226.aether.common.util.ImageUtils;
import com.xiaobai1226.aether.common.util.VideoUtils;
import com.xiaobai1226.aether.core.domain.vo.UploadFileVO;
import com.xiaobai1226.aether.core.domain.vo.UserFileVO;
import com.xiaobai1226.aether.core.domain.vo.UserFolderVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.enums.UserFileStatusEnum;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.util.LockManager;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import com.xiaobai1226.aether.dao.domain.entity.RecycleBinDO;
import com.xiaobai1226.aether.dao.domain.entity.StorageSourceDO;
import com.xiaobai1226.aether.dao.domain.entity.UserDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.data.annotation.Tran;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.CategoryEnum.OTHER;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.UploadStatusEnum.*;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 用户文件service实现类
 *
 * @author bai
 */
@Component
@Slf4j
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFileDO> implements UserFileService {

    @Db
    private UserFileMapper userFileMapper;

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private FileCache fileCache;

    @Inject
    private UserCache userCache;

    @Inject
    private UserService userService;

    @Inject
    private FileService fileService;

    @Inject
    private RecycleBinService recycleBinService;

    @Inject
    private StorageSourceService storageSourceService;

    @Override
    public UserFileDO getParentFolderByPath(final Long userId, Long parentId, String path) {
        if (StrUtil.isEmpty(path)) {
            return null;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] dirs = path.split("/");

        if (dirs.length == 0) {
            return null;
        }

        if (parentId == null) {
            parentId = 0L;
        }

        LambdaQueryChainWrapper<UserFileDO> lambdaQuery;
        UserFileDO userFileDO;
        for (int i = 0; i < dirs.length; i++) {
            lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
            userFileDO = lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId)
                    .eq(UserFileDO::getName, dirs[i]).eq(UserFileDO::getFileStatus, NORMAL.flag())
                    .eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag()).one();

            if (userFileDO == null) {
                break;
            } else if (i == dirs.length - 1) {
                return userFileDO;
            } else {
                parentId = userFileDO.getId();
            }
        }

        return null;
    }

    @Override
    public UserFileDO getParentFolderByPathOrCreate(final Long userId, UserFileDO parentUserFile, String path) {
        if (StrUtil.isEmpty(path)) {
            return null;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] dirs = path.split("/");

        if (dirs.length == 0) {
            return null;
        }

        LambdaQueryChainWrapper<UserFileDO> lambdaQuery;
        UserFileDO userFileDO = parentUserFile != null ? parentUserFile : new UserFileDO().setId(0L);

        // 是否新建
        var isCreate = false;
        var lockKey = "";
        for (int i = 0; i < dirs.length; i++) {
            lockKey = userFileDO.getId() + ":" + dirs[i];

            // 上锁
            LockManager.lock(lockKey);

            try {
                if (!isCreate) {
                    lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
                    userFileDO = lambdaQuery.eq(UserFileDO::getUserId, userId)
                            .eq(UserFileDO::getParentId, userFileDO.getId())
                            .eq(UserFileDO::getName, dirs[i]).eq(UserFileDO::getFileStatus, NORMAL.flag())
                            .eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag()).one();

                    if (userFileDO == null) {
                        userFileDO = newFolder(dirs[i], userFileDO, userId);
                        isCreate = true;
                    }
                } else {
                    userFileDO = newFolder(dirs[i], userFileDO, userId);
                }
            } finally {
                LockManager.unlock(lockKey);
            }

            if (i == dirs.length - 1) {
                return userFileDO;
            }
        }

        return null;
    }

    @Override
    public UserFileDTO getUserFileDTOByPath(final Long userId, String path) {
        if (StrUtil.isEmpty(path)) {
            return null;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] dirs = path.split("/");

        if (dirs.length == 0) {
            return null;
        }

        var parentId = 0L;

        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(NORMAL.flag());
        for (int i = 0; i < dirs.length; i++) {
            userFileDO.setParentId(parentId).setName(dirs[i]);

            // 查询文件列表
            var userFileDTO = userFileMapper.getUserFileDTOByNameAndParentId(userFileDO);

            if (userFileDTO == null) {
                break;
            } else if (i == dirs.length - 1) {
                return userFileDTO;
            } else {
                parentId = userFileDTO.getId();
            }
        }

        return null;
    }

    @Override
    public PageResult<UserFileDTO> getFileList(final Long userId, Long parentId, UserFileVO userFileVO) {
        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(NORMAL.flag());

        if (userFileVO == null) {
            userFileVO = new UserFileVO();
            userFileVO.setPageNum(1);
            userFileVO.setPageSize(-1);
        }

        Set<String> suffixSet = null;
        // 如果分类为全部分类，则不设置分类条件
        if (userFileVO.getCategory() != null) {
            if (userFileVO.getCategory().equals(OTHER.id())) {
                suffixSet = CategoryEnum.getAllSuffix();
            } else {
                suffixSet = CategoryEnum.getSuffixSet(userFileVO.getCategory());
            }
            userFileDO.setItemType(FILE.flag());
        } else {
            userFileDO.setParentId(parentId);
        }

        // 分页对象
        Page<UserFileDTO> page = new Page<>(userFileVO.getPageNum(), userFileVO.getPageSize());

        // 查询文件列表
        var userFileDTOList = userFileMapper.getFileListByPage(page, userFileDO, userFileVO.getCategory(), suffixSet,
                userFileVO.getSortingField(), userFileVO.getSortingMethod());

        // 判断结果是否为空
        if (CollUtil.isNotEmpty(userFileDTOList)) {
            page.setRecords(userFileDTOList);
            return new PageResult<>(page);
        }

        return null;
    }

    @Override
    // public UserFileDO getUserFileByName(String fileName, Integer userId, Integer
    // parentId, UserFileStatusEnum userFileStatus, UserFileItemTypeEnum itemType) {
    public UserFileDO getUserFileByName(String fileName, final Long userId, Long parentId,
            UserFileStatusEnum userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        // return lambdaQuery.eq(UserFileDO::getUserId,
        // userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getName,
        // fileName).eq(UserFileDO::getFileStatus,
        // userFileStatus.flag()).eq(UserFileDO::getItemType, itemType.flag()).one();
        return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId)
                .eq(UserFileDO::getName, fileName).eq(UserFileDO::getFileStatus, userFileStatus.flag()).one();
    }

    @Override
    public UserFileDO newFolder(String folderName, UserFileDO parentUserFileDO, final Long userId) {
        // 获取存储源ID
        Long storageSourceId = getStorageSourceIdByParent(parentUserFileDO, userId);
        // 父文件夹ID
        long parentId = parentUserFileDO != null ? parentUserFileDO.getId() : 0L;

        return addUserFile(userId, null, parentId, folderName, UserFileItemTypeEnum.FOLDER, NORMAL, null,
                storageSourceId);
    }

    @Override
    public UserFileDO getUserFileByIdAndUserId(Long id, final Long userId, UserFileStatusEnum userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getId, id);
        if (userFileStatus != null) {
            lambdaQuery.eq(UserFileDO::getFileStatus, userFileStatus.flag());
        }
        return lambdaQuery.one();
    }

    @Override
    public Boolean updateFileNameById(Long id, final Long userId, String newName, UserFileStatusEnum userFileStatus) {
        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFileDO::getName, newName).eq(UserFileDO::getId, id)
                .eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatus.flag());
        var updateNameResult = userFileMapper.update(null, lambdaUpdateWrapper);

        return updateNameResult == 1;
    }

    @Override
    public Boolean rename(Long id, final Long userId, String newName, UserFileDO userFileDO,
            UserFileStatusEnum userFileStatus) {
        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFileDO::getName, newName).eq(UserFileDO::getId, id)
                .eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatus.flag());

        if (UserFileItemTypeEnum.isFile(userFileDO.getItemType())) {
            var suffix = FileNameUtil.extName(newName);
            if (StrUtil.isNotEmpty(suffix)) {
                lambdaUpdateWrapper.set(UserFileDO::getSuffix, suffix.toLowerCase());
            } else {
                lambdaUpdateWrapper.set(UserFileDO::getSuffix, null);
            }
        }

        var updateNameResult = userFileMapper.update(null, lambdaUpdateWrapper);

        return updateNameResult == 1;
    }

    @Override
    public UploadResultDTO secondUploadFile(final Long userId, UserFileDO parentUserFile, UploadFileVO uploadFileVO,
            FileDO fileDO) {
        // 获取父文件夹的存储源ID
        Long storageSourceId = getStorageSourceIdByParent(parentUserFile, userId);

        // 插入数据库
        addUserFile(userId, fileDO.getId(), parentUserFile.getParentId(), uploadFileVO.getFileName(), FILE, NORMAL,
                fileDO.getSize(),
                storageSourceId);
        return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_SECOND.id());
    }

    @Override
    public FileDO trySecondUpload(final Long userId, UserFileDO parentUserFile, UploadFileVO uploadFileVO) {
        // TODO 检测存储空间是否足够
        // var userSpaceUsage = userService.getUserSpaceUsage(userId);
        // if (userSpaceUsage == null || userSpaceUsage.getRealRemainStorage() <
        // uploadFileVO.getFileSize()) {
        // throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
        // }

        // 获取目标存储源ID和存储源对象
        Long targetStorageSourceId = getStorageSourceIdByParent(parentUserFile, userId);
        if (targetStorageSourceId == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
        }

        var targetStorageSource = storageSourceService.getStorageSourceById(targetStorageSourceId, userId);
        if (targetStorageSource == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
        }

        // 检测文件是否已存在（获取所有相同identifier的文件列表）
        List<FileDO> existingFileList = fileService.getFileListByIdentifier(uploadFileVO.getIdentifier());

        // 若文件列表为空，返回null表示无法秒传
        if (CollUtil.isEmpty(existingFileList)) {
            return null;
        }

        // 查找是否有同存储源的文件
        FileDO storageFileDO = existingFileList.stream()
                .filter(f -> f.getStorageSourceId() != null
                        && f.getStorageSourceId().equals(targetStorageSourceId))
                .findFirst()
                .orElse(null);

        if (storageFileDO != null) {
            // 情况2：有同存储源的文件，直接秒传
            // TODO 如果前端传过来的文件大小，小于数据库中记录的，则重新判断空间是否足够
            // if (uploadFileVO.getFileSize() < sameStorageFileDO.getSize()) {
            // if (userSpaceUsage.getRealRemainStorage() < sameStorageFileDO.getSize()) {
            // throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
            // }
            // }

            return null;
        }

        // 情况3：有文件但没有同存储源的，从其他存储源复制文件
        FileDO sourceFileDO = existingFileList.get(0); // 选择第一个作为源文件
        var sourceStorageSourceDO = storageSourceService.getStorageSourceById(sourceFileDO.getStorageSourceId(),
                userId);

        // 复制文件到目标存储源
        storageFileDO = fileService.copyFileToStorageSource(sourceFileDO, sourceStorageSourceDO.getPath(),
                targetStorageSource.getPath(), targetStorageSourceId);

        // TODO 如果前端传过来的文件大小，小于数据库中记录的，则重新判断空间是否足够
        // if (uploadFileVO.getFileSize() < newFileDO.getSize()) {
        // if (userSpaceUsage.getRealRemainStorage() < newFileDO.getSize()) {
        // throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
        // }
        // }

        // 需要执行秒传操作
        return storageFileDO;
    }

    @Tran
    @Override
    public UploadResultDTO splitUploadFile(UploadedFile file, final Long userId, UserFileDO parentUserFile,
            UploadFileVO uploadFileVO, UploadFileCacheDTO uploadFileCacheDTO) throws IOException {
        UploadFileTempDTO uploadTempFileDTO;

        // 设置暂存临时目录（使用配置的rootPath）
        var tempFolder = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, userId.toString(),
                uploadFileVO.getTaskId());
        var tempDir = FileUtil.file(tempFolder);
        uploadFileCacheDTO.setTempDir(tempDir);

        long parentId = parentUserFile != null ? parentUserFile.getId() : 0L;

        // 如果是第一片
        if (uploadFileVO.getChunkIndex() == 0) {
            if (file.getContentSize() > uploadFileVO.getFileSize()) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_FILE_SIZE_OVERFLOW);
            }

            // 增加上传中文件大小（整个文件大小）
            userCache.incrementUploadingFileSize(userId, uploadFileVO.getFileSize());

            // 切片是0，则表示redis中还没有数据，要新增
            var uploadFileTempDTO = BeanUtil.toBean(uploadFileVO, UploadFileTempDTO.class);
            uploadFileTempDTO.setUploadedSize(0L);
            uploadFileTempDTO.setTempFolder(tempFolder);
            uploadFileTempDTO.setParentId(parentId);
            fileCache.putUploadTempFileInfo(userId, uploadFileVO.getTaskId(), uploadFileTempDTO);

            // 如果文件夹不存在则创建目录
            if (!FileUtil.isDirectory(tempDir)) {
                if (!FileUtil.exist(tempDir)) {
                    FileUtil.mkdir(tempDir);
                }
            }
        } else {
            // 获取缓存中数据
            // var uploadTempFileDTO = fileCache.getUploadTempFileInfo(userId,
            // uploadFileVO.getTaskId());
            //
            // // 如果缓存中没有数据，则返回上传失败
            // if (uploadTempFileDTO == null) {
            // // 删除缓存数据
            // fileCache.delUploadTempFileInfo(userId, uploadFileVO.getTaskId());
            // userCache.decrementUploadingFileSize(userId, uploadFileVO.getFileSize());
            // FileUtil.del(tempDir);
            //
            // return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_FAIL.id());
            // }
            //
            // // 如果缓存中有数据，但是实际上传文件大小已超过初始文件大小
            // if (uploadTempFileDTO.getFileSize() < (uploadTempFileDTO.getUploadedSize() +
            // file.getSize())) {
            // // 删除缓存数据
            // fileCache.delUploadTempFileInfo(userId, uploadFileVO.getTaskId());
            // userCache.decrementUploadingFileSize(userId, uploadFileVO.getFileSize());
            // FileUtil.del(tempDir);
            //
            // return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_FAIL.id());
            // }

            // 不是第一片，增加uploadedSize
            fileCache.updateUploadedSize(userId, uploadFileVO.getTaskId(), file.getContentSize());
        }

        // 将文件写入临时目录
        File tempFile = FileUtil.file(tempDir, uploadFileVO.getChunkIndex().toString());
        file.transferTo(tempFile);

        // 如果不是最后一片，直接返回上传中
        if (uploadFileVO.getChunkIndex() < uploadFileVO.getTotalChunks() - 1) {
            return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOADING.id());
        }

        // 获取缓存中数据
        uploadTempFileDTO = fileCache.getUploadTempFileInfo(userId, uploadFileVO.getTaskId());

        // 获取存储源（用于文件存储）
        var storageSourceId = getStorageSourceIdByParent(parentUserFile, userId);
        if (storageSourceId == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
        }

        var storageSource = storageSourceService.getStorageSourceById(storageSourceId, userId);
        if (storageSource == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
        }

        // 如果是最后一片，执行合并分片操作（文件存储到存储源）
        var finalFilePath = fileService.mergeFile(uploadTempFileDTO.getFileName(), uploadFileVO.getTaskId(), tempFolder,
                storageSource.getPath());
        var finalFullFilePath = FileUtils.generatePath(storageSource.getPath(), finalFilePath);
        uploadFileCacheDTO.setFinalFilePath(finalFilePath);

        // 获取最终文件
        var finalFile = FileUtil.file(finalFullFilePath);
        // 获取最终文件大小
        var finalFileSize = FileUtil.size(finalFile);
        var finalFileName = finalFile.getName();

        // 如果最终文件大小，大于初始文件大小
        if (finalFileSize > uploadTempFileDTO.getFileSize()) {
            // 重新检测文件大小是否足够
            var userSpaceUsage = userService.getUserSpaceUsage(userId);
            // 如果空间不足则返回上传失败
            if (userSpaceUsage.getRealRemainStorage() < (finalFileSize - uploadTempFileDTO.getFileSize())) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
            }
        }

        var thumbnailSuffix = FileTypeEnum.isGif(FileNameUtil.extName(finalFileName).toLowerCase())
                ? SystemConsts.THUMBNAIL_GIF_SUFFIX
                : SystemConsts.THUMBNAIL_SUFFIX;
        String thumbnailFileName = DateUtil.format(new Date(), "yyyy/MM/dd") + StrUtil.SLASH
                + FileUtils.replaceFileExtName(finalFileName, thumbnailSuffix);
        // 设置缩略图存储全路径（使用配置的rootPath）
        var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL,
                thumbnailFileName);
        uploadFileCacheDTO.setThumbnailFilePath(thumbnailFilePath);

        // 图片生成缩略图
        if (CategoryEnum.isPictureByName(uploadTempFileDTO.getFileName())) {
            var result = ImageUtils.generateThumbnail(finalFullFilePath, thumbnailFilePath, 150, -1);
            thumbnailFileName = result ? thumbnailFileName : null;
        } else if (CategoryEnum.isVideoByName(uploadTempFileDTO.getFileName())) { // 视频生成缩略图
            var result = VideoUtils.generateThumbnail(finalFullFilePath, thumbnailFilePath, 150);
            thumbnailFileName = result ? thumbnailFileName : null;
        } else {
            thumbnailFileName = null;
        }

        if (thumbnailFileName == null) {
            uploadFileCacheDTO.setThumbnailFilePath(null);
        }

        // 写入File库，获取文件ID（storageSourceId已在前面获取）
        var fileDO = fileService.addFile(finalFileName, finalFilePath, finalFileSize, uploadTempFileDTO.getIdentifier(),
                thumbnailFileName, storageSourceId);

        if (fileDO == null) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 插入数据库
        addUserFile(userId, fileDO.getId(), parentId, uploadFileVO.getFileName(), FILE, NORMAL, finalFileSize,
                storageSourceId);
        // 删除缓存数据
        fileCache.delUploadTempFileInfo(userId, uploadFileVO.getTaskId());
        userCache.decrementUploadingFileSize(userId, uploadTempFileDTO.getFileSize());
        FileUtil.del(tempDir);

        return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_FINISH.id());
    }

    @Override
    public void cancelUploadFile(final Long userId, String taskId) {

        var uploadTempFileInfo = fileCache.getUploadTempFileInfo(userId, taskId);

        if (uploadTempFileInfo == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_CANCEL_UPLOAD);
        }
        // 删除缓存数据
        fileCache.delUploadTempFileInfo(userId, taskId);
        userCache.decrementUploadingFileSize(userId, uploadTempFileInfo.getFileSize());
        var tempFolder = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, userId, taskId);
        var tempDir = FileUtil.file(tempFolder);
        FileUtil.del(tempDir);
    }

    @Override
    public void clearUploadFileCache(final Long userId, String taskId, Long fileSize,
            UploadFileCacheDTO uploadFileCacheDTO) {
        // 删除缓存数据
        fileCache.delUploadTempFileInfo(userId, taskId);
        userCache.decrementUploadingFileSize(userId, fileSize);
        FileUtil.del(uploadFileCacheDTO.getTempDir());
        FileUtil.del(uploadFileCacheDTO.getFinalFilePath());
        FileUtil.del(uploadFileCacheDTO.getThumbnailFilePath());
    }

    @Override
    public PageResult<UserFileDO> getFolderList(final Long userId, Long parentId, UserFolderVO userFolderVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        var userFolderListPage = lambdaQuery.eq(UserFileDO::getUserId, userId)
                .eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag())
                .eq(UserFileDO::getFileStatus, NORMAL.flag()).eq(UserFileDO::getParentId, parentId)
                .orderByDesc(UserFileDO::getUpdateTime)
                .page(new Page<>(userFolderVO.getPageNum(), userFolderVO.getPageSize()));

        // 判断结果是否为空
        if (CollUtil.isNotEmpty(userFolderListPage.getRecords())) {
            return new PageResult<>(userFolderListPage);
        }

        return null;
    }

    @Override
    public List<UserFileDO> getUserFileByIdsAndUserId(List<Long> ids, final Long userId,
            UserFileStatusEnum userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        return lambdaQuery.eq(UserFileDO::getUserId, userId).in(UserFileDO::getId, ids)
                .eq(UserFileDO::getFileStatus, userFileStatus.flag()).list();
    }

    @Override
    public List<Long> getAllSubfolders(final Long userId, List<Long> ids) {

        var totalSubfolderIds = new ArrayList<Long>();

        var userFolderVO = new UserFolderVO();
        userFolderVO.setPageNum(1);
        // 小于0 表示不分页
        userFolderVO.setPageSize(-1);
        for (var id : ids) {
            var subfoldersPage = getFolderList(userId, id, userFolderVO);

            if (subfoldersPage == null || CollUtil.isEmpty(subfoldersPage.getList())) {
                continue;
            }

            var tempSubfolderIds = new ArrayList<Long>();
            for (var subfolder : subfoldersPage.getList()) {
                totalSubfolderIds.add(subfolder.getId());
                tempSubfolderIds.add(subfolder.getId());
            }

            var tempTotalSubfolderIds = getAllSubfolders(userId, tempSubfolderIds);

            if (CollUtil.isNotEmpty(tempTotalSubfolderIds)) {
                totalSubfolderIds.addAll(tempTotalSubfolderIds);
            }
        }

        return totalSubfolderIds;
    }

    @Override
    public Long getCountByNames(List<String> fileNames, final Long userId, Long parentId,
            UserFileStatusEnum userFileStatus, UserFileItemTypeEnum itemType) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId)
                .in(UserFileDO::getName, fileNames).eq(UserFileDO::getFileStatus, userFileStatus.flag())
                .eq(UserFileDO::getItemType, itemType.flag()).count();
    }

    @Tran
    @Override
    public void updateParentIdByIds(List<Long> sourceIds, Long targetId, final Long userId,
            UserFileStatusEnum userFileStatus) {
        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFileDO::getParentId, targetId).in(UserFileDO::getId, sourceIds)
                .eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatus.flag());
        var updateNameCount = userFileMapper.update(null, lambdaUpdateWrapper);

        if (updateNameCount != sourceIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    @Override
    public List<UserFileDTO> getUserFileDTOListByIds(List<Long> ids, final Long userId,
            UserFileStatusEnum userFileStatus) {
        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(userFileStatus.flag());
        var userFileDTOList = userFileMapper.getUserFileDTOByIds(userFileDO, ids);
        if (CollUtil.isEmpty(userFileDTOList)) {
            return null;
        }

        return userFileDTOList;
    }

    @Override
    public List<UserFileTreeDTO> getUserFileTreeListByIds(List<Long> ids, final Long userId,
            UserFileStatusEnum userFileStatus) {
        var userFileDTOList = getUserFileDTOListByIds(ids, userId, userFileStatus);
        if (CollUtil.isEmpty(userFileDTOList)) {
            return null;
        }

        return BeanUtil.copyToList(userFileDTOList, UserFileTreeDTO.class);
    }

    @Override
    public void getSubUserFileTree(final Long userId, List<UserFileTreeDTO> userFileTreeList) {
        UserFileVO userFileVO;
        List<UserFileTreeDTO> subUserFileTreeList;
        for (var userFileTree : userFileTreeList) {
            if (UserFileItemTypeEnum.isFile(userFileTree.getItemType())) {
                continue;
            }

            userFileVO = new UserFileVO();
            userFileVO.setPageNum(1);
            userFileVO.setPageSize(-1);
            var userFileDTOListPage = getFileList(userId, userFileTree.getId(), userFileVO);

            if (userFileDTOListPage == null || CollUtil.isEmpty(userFileDTOListPage.getList())) {
                continue;
            }

            subUserFileTreeList = BeanUtil.copyToList(userFileDTOListPage.getList(), UserFileTreeDTO.class);
            userFileTree.setChildUserFileDTOList(subUserFileTreeList);
            getSubUserFileTree(userId, subUserFileTreeList);
        }
    }

    @Override
    public Long getUserFileTreeSpaceUsage(List<UserFileTreeDTO> userFileTreeDTOList) {
        var totalSize = 0L;
        for (var userFileTree : userFileTreeDTOList) {
            if (UserFileItemTypeEnum.isFile(userFileTree.getItemType())) {
                totalSize += userFileTree.getSize();
                continue;
            }

            var childrenUserFileDTOList = userFileTree.getChildUserFileDTOList();
            if (CollUtil.isNotEmpty(childrenUserFileDTOList)) {
                var childrenTotalSize = getUserFileTreeSpaceUsage(childrenUserFileDTOList);
                totalSize += childrenTotalSize;
            }
        }

        return totalSize;
    }

    @Tran
    @Override
    public void copy(Long targetId, final Long userId, List<UserFileTreeDTO> sourceUserFileTreeDTOList,
            Long totalSize) {
        if (totalSize > 0) {
            // 增加上传中文件大小（整个文件大小）
            userCache.incrementUploadingFileSize(userId, totalSize);
        }

        insertUserFileTree(sourceUserFileTreeDTOList, userId, targetId);

        // 更新用户所使用的空间
        if (totalSize > 0) {
            // 更新用户已使用存储空间
            var userSpaceUsageDTO = userService.getUserSpaceUsage(userId);
            var userDO = new UserDO();
            userDO.setId(userId);
            userDO.setUsedStorage(userSpaceUsageDTO.getUsedStorage() + totalSize);
            userService.updateUser(userDO);

            // 减去上传中文件大小（整个文件大小）
            userCache.decrementUploadingFileSize(userId, totalSize);
        }
    }

    @Tran
    @Override
    public void delete(List<UserFileTreeDTO> delUserFileTreeList, final Long userId) {
        var delIds = new ArrayList<Long>();
        var recycleBinDOList = new ArrayList<RecycleBinDO>();

        // 获取全部删除信息
        getDelInfo(delUserFileTreeList, delIds, recycleBinDOList, userId, null, 1);

        // 修改文件状态为回收站
        updateUserFileStatusById(delIds, userId, UserFileStatusEnum.DEL);

        // 新增回收站数据
        var insertBatchResult = recycleBinService.insertBatch(recycleBinDOList);

        if (!insertBatchResult) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // TODO 定好回收站是否占用用户空间后，调整用户空间修改逻辑
    }

    @Tran
    @Override
    public void updateUserFileStatusById(List<Long> ids, final Long userId, UserFileStatusEnum userFileStatus) {
        var lambdaUpdateWrapper = new LambdaUpdateWrapper<UserFileDO>();
        lambdaUpdateWrapper.set(UserFileDO::getFileStatus, userFileStatus.flag()).eq(UserFileDO::getUserId, userId)
                .in(UserFileDO::getId, ids);
        var updateFileStatusCount = userFileMapper.update(null, lambdaUpdateWrapper);

        if (ids.size() != updateFileStatusCount) {
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 新增一条UserFile到数据库
     *
     * @param userId             用户ID
     * @param fileId             文件ID
     * @param parentId           父ID
     * @param fileName           文件名称
     * @param userFileItemType   条目类型
     * @param userFileStatusEnum 文件状态
     * @param fileSize           文件大小
     * @param storageSourceId    存储源ID
     */
    @Tran
    private UserFileDO addUserFile(Long userId, Long fileId, Long parentId, String fileName,
            UserFileItemTypeEnum userFileItemType, UserFileStatusEnum userFileStatusEnum, Long fileSize,
            Long storageSourceId) {

        var userFileDO = new UserFileDO();

        userFileDO.setUserId(userId);
        userFileDO.setItemType(userFileItemType.flag());
        userFileDO.setFileStatus(userFileStatusEnum.flag());
        userFileDO.setParentId(parentId);
        userFileDO.setStorageSourceId(storageSourceId);

        // 根据文件名称获取文件
        // var sameNameUserFile = getUserFileByName(fileName, userId,
        // userFileDO.getParentId(), userFileStatusEnum, userFileItemType);
        var sameNameUserFile = getUserFileByName(fileName, userId, userFileDO.getParentId(), userFileStatusEnum);
        // 如果不为null，则说明同名文件已存在，进行改名
        if (sameNameUserFile != null) {
            // 重命名文件名称
            fileName = FileUtils.rename(fileName);
        }
        userFileDO.setName(fileName);

        if (UserFileItemTypeEnum.isFile(userFileItemType)) {
            userFileDO.setFileId(fileId);

            var suffix = FileNameUtil.extName(fileName);
            if (StrUtil.isNotEmpty(suffix)) {
                userFileDO.setSuffix(suffix.toLowerCase());
            }
        }

        var resultCount = userFileMapper.insert(userFileDO);

        if (resultCount != 1) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        if (UserFileItemTypeEnum.isFile(userFileItemType)) {
            // 更新用户已使用存储空间
            var userSpaceUsageDTO = userService.getUserSpaceUsage(userId);
            var userDO = new UserDO();
            userDO.setId(userId);
            userDO.setUsedStorage(userSpaceUsageDTO.getUsedStorage() + fileSize);
            var result = userService.updateUser(userDO);

            if (result != 1) {
                throw new FailResultException(SYSTEM_ERROR);
            }
        }

        return userFileDO;
    }

    @Override
    public Long getStorageSourceIdByParent(UserFileDO parentUserFile, Long userId) {

        StorageSourceDO storageSource = null;

        // 如果是根目录，使用默认存储源
        if (parentUserFile == null) {
            storageSource = storageSourceService.getDefaultStorageSource(userId);
            if (storageSource == null) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
            }

            return storageSource.getId();
        }

        if (parentUserFile.getStorageSourceId() == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
        }

        return parentUserFile.getStorageSourceId();
    }

    @Override
    @Tran
    public boolean setFolderStorageSource(Long folderId, Long storageSourceId, final Long userId) {
        // 检查文件夹是否存在
        var folder = getUserFileByIdAndUserId(folderId, userId, NORMAL);
        if (folder == null || !UserFileItemTypeEnum.isFolder(folder.getItemType())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        // 检查存储源是否存在
        var storageSource = storageSourceService.getStorageSourceById(storageSourceId, userId);
        if (storageSource == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_STORAGE_SOURCE_NO_EXIST);
        }

        // 如果存储源没有变化，直接返回
        if (folder.getStorageSourceId() != null && folder.getStorageSourceId().equals(storageSourceId)) {
            return true;
        }

        // 获取旧存储源
        var oldStorageSource = folder.getStorageSourceId() != null
                ? storageSourceService.getStorageSourceById(folder.getStorageSourceId(), userId)
                : null;

        try {
            // 1. 递归获取该文件夹下所有文件
            var fileIdsToMigrate = new ArrayList<Long>();
            collectFileIdsRecursively(folderId, userId, fileIdsToMigrate);

            // 2. 迁移文件
            if (CollUtil.isNotEmpty(fileIdsToMigrate) && oldStorageSource != null) {
                migrateFiles(fileIdsToMigrate, oldStorageSource.getPath(), storageSource.getPath());
            }

            // 3. 更新文件夹及其所有子文件夹的存储源ID
            updateFolderStorageSourceRecursively(folderId, storageSourceId);

            return true;
        } catch (Exception e) {
            log.error("设置文件夹存储源失败", e);
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_FILE_MIGRATION);
        }
    }

    /**
     * 递归收集文件夹下所有文件的ID
     *
     * @param folderId 文件夹ID
     * @param userId   用户ID
     * @param fileIds  收集的文件ID列表
     */
    private void collectFileIdsRecursively(Long folderId, Long userId, List<Long> fileIds) {
        var userFiles = ChainWrappers.lambdaQueryChain(userFileMapper)
                .eq(UserFileDO::getUserId, userId)
                .eq(UserFileDO::getParentId, folderId)
                .eq(UserFileDO::getFileStatus, NORMAL.flag())
                .list();

        for (var userFile : userFiles) {
            if (UserFileItemTypeEnum.isFile(userFile.getItemType())) {
                if (userFile.getFileId() != null) {
                    fileIds.add(userFile.getFileId());
                }
            } else {
                // 递归处理子文件夹
                collectFileIdsRecursively(userFile.getId(), userId, fileIds);
            }
        }
    }

    /**
     * 迁移文件
     *
     * @param fileIds        文件ID列表
     * @param oldStoragePath 旧存储路径
     * @param newStoragePath 新存储路径
     */
    private void migrateFiles(List<Long> fileIds, String oldStoragePath, String newStoragePath) {
        for (var fileId : fileIds) {
            var fileDO = fileService.getFileById(fileId);
            if (fileDO == null) {
                continue;
            }

            // 构造旧文件路径和新文件路径
            var oldFilePath = FileUtils.generatePath(oldStoragePath, fileDO.getPath());
            var newFilePath = FileUtils.generatePath(newStoragePath, fileDO.getPath());

            // 复制文件
            var oldFile = FileUtil.file(oldFilePath);
            if (!oldFile.exists()) {
                log.warn("文件不存在: {}", oldFilePath);
                continue;
            }

            var newFile = FileUtil.file(newFilePath);
            // 确保目录存在
            FileUtil.mkParentDirs(newFile);

            // 复制文件
            FileUtil.copy(oldFile, newFile, true);

            // 验证复制是否成功
            if (!newFile.exists() || newFile.length() != oldFile.length()) {
                throw new RuntimeException("文件复制失败: " + fileDO.getPath());
            }

            // 删除旧文件
            FileUtil.del(oldFile);

            // 缩略图存储在rootPath，不需要迁移
        }
    }

    /**
     * 递归更新文件夹及其子文件夹的存储源ID
     *
     * @param folderId        文件夹ID
     * @param storageSourceId 存储源ID
     */
    private void updateFolderStorageSourceRecursively(Long folderId, Long storageSourceId) {
        // 更新当前文件夹
        var lambdaUpdate = new LambdaUpdateWrapper<UserFileDO>();
        lambdaUpdate.set(UserFileDO::getStorageSourceId, storageSourceId)
                .eq(UserFileDO::getId, folderId);
        userFileMapper.update(null, lambdaUpdate);

        // 获取所有子文件夹
        var subFolders = ChainWrappers.lambdaQueryChain(userFileMapper)
                .eq(UserFileDO::getParentId, folderId)
                .eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag())
                .eq(UserFileDO::getFileStatus, NORMAL.flag())
                .list();

        // 递归更新子文件夹
        for (var subFolder : subFolders) {
            updateFolderStorageSourceRecursively(subFolder.getId(), storageSourceId);
        }
    }

    /**
     * 新增UserFileTree结构对象
     *
     * @param userFileTreeList 要插入的元素集合
     * @param userId           用户ID
     * @param parentId         父文件夹ID
     */
    @Tran
    private void insertUserFileTree(List<UserFileTreeDTO> userFileTreeList, Long userId, Long parentId) {
        if (CollUtil.isEmpty(userFileTreeList)) {
            return;
        }

        var userFileList = new ArrayList<UserFileDO>();
        for (var userFileTree : userFileTreeList) {
            var userFileDO = new UserFileDO();
            BeanUtil.copyProperties(userFileTree, userFileDO);

            userFileDO.setId(null);
            userFileDO.setUserId(userId);
            userFileDO.setParentId(parentId);
            userFileDO.setCreateTime(null);
            userFileDO.setUpdateTime(null);

            userFileList.add(userFileDO);
        }

        var saveBatchResult = this.saveBatch(userFileList);

        if (!saveBatchResult) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        UserFileTreeDTO userFileTree;
        for (int i = 0; i < userFileTreeList.size(); i++) {
            userFileTree = userFileTreeList.get(i);
            if (UserFileItemTypeEnum.isFile(userFileTree.getItemType())
                    || CollUtil.isEmpty(userFileTree.getChildUserFileDTOList())) {
                continue;
            }

            // 插入子节点对象
            insertUserFileTree(userFileTree.getChildUserFileDTOList(), userId, userFileList.get(i).getId());
        }
    }

    /**
     * 获取全部要删除的对象
     *
     * @param userFileTreeList 要删除的元素集合
     * @param delIds           要删除的元素ID集合
     * @param recycleBinList   回收站元素集合
     * @param userId           用户ID
     * @param recycleId        回收ID
     * @param root             是否是根节点 1 是根节点，0 不是
     */
    private void getDelInfo(List<UserFileTreeDTO> userFileTreeList, List<Long> delIds,
            List<RecycleBinDO> recycleBinList, Long userId, String recycleId, Integer root) {
        if (CollUtil.isEmpty(userFileTreeList)) {
            return;
        }

        RecycleBinDO recycleBinDO;
        var tempRecycleId = recycleId;
        for (var userFileTree : userFileTreeList) {
            delIds.add(userFileTree.getId());

            recycleBinDO = new RecycleBinDO();
            if (StrUtil.isEmpty(recycleId)) {
                tempRecycleId = IdUtil.randomUUID();
            }
            recycleBinDO.setRecycleId(tempRecycleId);
            recycleBinDO.setRoot(root);
            recycleBinDO.setUserFileId(userFileTree.getId());
            recycleBinDO.setUserId(userId);

            recycleBinList.add(recycleBinDO);

            if (UserFileItemTypeEnum.isFile(userFileTree.getItemType())
                    || CollUtil.isEmpty(userFileTree.getChildUserFileDTOList())) {
                continue;
            }

            // 递归删除子节点对象
            getDelInfo(userFileTree.getChildUserFileDTOList(), delIds, recycleBinList, userId, tempRecycleId, 0);
        }
    }

    @Override
    public List<UserFileDO> getUserFileListByUserIdAndParentId(final Long userId, Long parentId,
            Integer userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId)
                .eq(UserFileDO::getFileStatus, userFileStatus).list();
    }

    @Override
    public UserFileDO getParentUserFileByPathAndItemType(final Long userId, String path) {
        if (StrUtil.isEmpty(path)) {
            return null;
        }

        String[] dirs = path.split("/");

        if (dirs.length == 0) {
            return null;
        }

        var parentId = 0L;
        for (int i = 1; i < dirs.length; i++) {
            var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
            var userFileDO = lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId)
                    .eq(UserFileDO::getName, dirs[i]).eq(UserFileDO::getFileStatus, NORMAL.flag())
                    .eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag()).one();
            if (userFileDO == null) {
                break;
            } else if (i == dirs.length - 1) {
                return userFileDO;
            } else {
                parentId = userFileDO.getId();
            }
        }

        return null;
    }

    @Override
    public List<UserFileTreeDTO> getUserFileTreeDTOByIdsAndUserId(List<Long> ids, final Long userId,
            Integer userFileStatus) {
        var userFileDO = new UserFileDO();
        userFileDO.setUserId(userId);
        userFileDO.setFileStatus(userFileStatus);
        return userFileMapper.getUserFileTreeDTOByIdsAndUserId(userFileDO, ids);
    }

    @Override
    public void recursiveGetUserFileTreeDTO(List<UserFileTreeDTO> sourceUserFileTreeDTOList, final Long userId) {
        for (UserFileTreeDTO sourceUserFileTreeDTO : sourceUserFileTreeDTOList) {

            if (Objects.equals(FILE.flag(), sourceUserFileTreeDTO.getItemType())) {
                continue;
            }

            var childrenUserFileDTOList = recursiveGetChildrenFile(sourceUserFileTreeDTO, userId);

            if (childrenUserFileDTOList != null && !childrenUserFileDTOList.isEmpty()) {
                sourceUserFileTreeDTO.setChildUserFileDTOList(childrenUserFileDTOList);
            }

        }
    }

    /**
     * 递归获取文件对象
     *
     * @param userId          用户ID
     * @param userFileTreeDTO 父文件对象
     */
    private List<UserFileTreeDTO> recursiveGetChildrenFile(UserFileTreeDTO userFileTreeDTO, Long userId) {

        var userFileDO = new UserFileDO();
        userFileDO.setUserId(userId);
        userFileDO.setFileStatus(NORMAL.flag());
        userFileDO.setParentId(userFileTreeDTO.getId());
        var childUserFileTreeDTOList = userFileMapper.getUserFileDTOByParentIdAndUserId(userFileDO);

        if (childUserFileTreeDTOList == null || childUserFileTreeDTOList.isEmpty()) {
            return null;
        }

        for (UserFileTreeDTO childUserFileTreeDTO : childUserFileTreeDTOList) {
            if (Objects.equals(FILE.flag(), childUserFileTreeDTO.getItemType())) {
                continue;
            }

            var childrenUserFileDTOList = recursiveGetChildrenFile(childUserFileTreeDTO, userId);

            if (childrenUserFileDTOList != null && !childrenUserFileDTOList.isEmpty()) {
                childUserFileTreeDTO.setChildUserFileDTOList(childrenUserFileDTOList);
            }
        }

        return childUserFileTreeDTOList;
    }

    // @Override
    // public Integer updateFilePathByParentId(Integer parentId, Integer userId,
    // String newPath, UserFileStatusEnum userFileStatusEnum) {
    // LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new
    // LambdaUpdateWrapper<>();
    // lambdaUpdateWrapper.set(UserFileDO::getPath,
    // newPath).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getUserId,
    // userId).eq(UserFileDO::getFileStatus, userFileStatusEnum.flag());
    // return userFileMapper.update(null, lambdaUpdateWrapper);
    // }

    /**
     * 根据文件ID获取文件数据
     *
     * @param id             文件或文件夹ID
     * @param userId         用户ID
     * @param userFileStatus 文件状态 1 正常 -1 删除 null 全部
     * @return 文件夹数据
     * @author bai
     */
    @Override
    public UserFileDO getUserFileById(Long id, final Long userId, Integer userFileStatus) {
        // try {
        // var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        // return lambdaQuery.eq(UserFileDO::getUserId,
        // userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getItemType,
        // itemType).eq(UserFileDO::getName, name).count();
        //
        // return userFileMapper. .getUserFileById(id, userId, userFileStatus);
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }

        return null;
    }

    /**
     * 根据文件路径模糊查询文件数据
     *
     * @param filePath       文件或文件夹路径
     * @param userId         用户ID
     * @param userFileStatus 文件状态 1 正常 -1 删除
     * @return 文件夹数据
     * @author bai
     */
    @Override
    public List<UserFileDO> getUserFileByLikeFilePath(String filePath, final Long userId, Integer userFileStatus) {
        // try {
        // return userFileDao.getUserFileByLikeFilePath(filePath, userId,
        // userFileStatus);
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }

        return null;
    }

    @Override
    public DownloadedFile download(List<UserFileTreeDTO> userFileTreeDTOList, final Long userId) throws IOException {
        if (userFileTreeDTOList.size() == 1
                && UserFileItemTypeEnum.isFile(userFileTreeDTOList.getFirst().getItemType())) {
            return new DownloadedFile(
                    new File(FileUtils.generatePath(rootPath, userFileTreeDTOList.getFirst().getPath())),
                    userFileTreeDTOList.getFirst().getName());
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                zipFiles(userFileTreeDTOList, zos, null, true);
            }

            var name = "打包下载.zip";
            if (userFileTreeDTOList.size() == 1) {
                name = userFileTreeDTOList.getFirst().getName() + "." + "zip";
            }

            return new DownloadedFile("application/zip", new ByteArrayInputStream(baos.toByteArray()), name);
        }
    }

    /**
     * 递归获取文件并压缩
     *
     * @param userFileTreeDTOList 用户文件
     * @param zipOutputStream
     * @param parentPath          父路径
     */
    private void zipFiles(List<UserFileTreeDTO> userFileTreeDTOList, ZipOutputStream zipOutputStream, String parentPath,
            Boolean isRoot) {
        for (var userFileTreeDTO : userFileTreeDTOList) {
            var fileFullPath = parentPath == null ? userFileTreeDTO.getName()
                    : FileUtils.generatePath(parentPath, userFileTreeDTO.getName());

            try {
                if (UserFileItemTypeEnum.isFolder(userFileTreeDTO.getItemType())) {
                    if (isRoot && userFileTreeDTOList.size() == 1) {
                        fileFullPath = null;
                    } else {
                        // 创建 ZipEntry 对象
                        ZipEntry zipEntry = new ZipEntry(fileFullPath + "/");
                        zipOutputStream.putNextEntry(zipEntry);
                    }

                    if (CollUtil.isNotEmpty(userFileTreeDTO.getChildUserFileDTOList())) {
                        zipFiles(userFileTreeDTO.getChildUserFileDTOList(), zipOutputStream, fileFullPath, false);
                    }
                } else {
                    // 创建 ZipEntry 对象
                    ZipEntry zipEntry = new ZipEntry(fileFullPath);
                    zipOutputStream.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    try (FileInputStream fileInputStream = new FileInputStream(
                            FileUtils.generatePath(rootPath, userFileTreeDTO.getPath()))) {

                        int length;
                        while ((length = fileInputStream.read(bytes)) >= 0) {
                            zipOutputStream.write(bytes, 0, length);
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }

                zipOutputStream.closeEntry();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}