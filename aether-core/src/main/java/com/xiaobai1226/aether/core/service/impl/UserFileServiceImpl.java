package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.solon.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.solon.plugins.pagination.Page;
import com.baomidou.mybatisplus.solon.service.impl.ServiceImpl;
import com.xiaobai1226.aether.common.enums.CategoryEnum;
import com.xiaobai1226.aether.core.constant.FolderNameConsts;
import com.xiaobai1226.aether.core.dao.redis.FileRedisDAO;
import com.xiaobai1226.aether.core.dao.redis.UserRedisDAO;
import com.xiaobai1226.aether.core.domain.dto.*;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.FileDO;
import com.xiaobai1226.aether.domain.entity.RecycleBinDO;
import com.xiaobai1226.aether.domain.entity.UserDO;
import com.xiaobai1226.aether.domain.entity.UserFileDO;
import com.xiaobai1226.aether.core.domain.vo.UploadFileVO;
import com.xiaobai1226.aether.core.domain.vo.UserFileVO;
import com.xiaobai1226.aether.core.domain.vo.UserFolderVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.enums.UserFileStatusEnum;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.mapper.UserFileMapper;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.core.util.FileUtils;
import com.xiaobai1226.aether.core.util.LockManager;
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
    private FileRedisDAO fileRedisDAO;

    @Inject
    private UserRedisDAO userRedisDAO;

    @Inject
    private UserService userService;

    @Inject
    private FileService fileService;

    @Inject
    private RecycleBinService recycleBinService;

    @Override
    public UserFileDO getParentFolderByPath(Integer userId, Integer parentId, String path) {
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
            parentId = 0;
        }

        LambdaQueryChainWrapper<UserFileDO> lambdaQuery;
        UserFileDO userFileDO;
        for (int i = 0; i < dirs.length; i++) {
            lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
            userFileDO = lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getName, dirs[i]).eq(UserFileDO::getFileStatus, NORMAL.flag()).eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag()).one();

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
    public Integer getParentFolderByPathOrCreate(Integer userId, Integer parentId, String path) {
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
            parentId = 0;
        }

        LambdaQueryChainWrapper<UserFileDO> lambdaQuery;
        UserFileDO userFileDO;
        // 是否新建
        var isCreate = false;
        var lockKey = "";
        for (int i = 0; i < dirs.length; i++) {
            lockKey = parentId + ":" + dirs[i];

            // 上锁
            LockManager.lock(lockKey);

            try {
                if (!isCreate) {
                    lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
                    userFileDO = lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getName, dirs[i]).eq(UserFileDO::getFileStatus, NORMAL.flag()).eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag()).one();

                    if (userFileDO == null) {
                        parentId = newFolder(dirs[i], parentId, userId);
                        isCreate = true;
                    } else {
                        parentId = userFileDO.getId();
                    }
                } else {
                    parentId = newFolder(dirs[i], parentId, userId);
                }
            } finally {
                LockManager.unlock(lockKey);
            }

            if (i == dirs.length - 1) {
                return parentId;
            }
        }

        return null;
    }

    @Override
    public UserFileDTO getUserFileDTOByPath(Integer userId, String path) {
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

        var parentId = 0;

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
    public PageResult<UserFileDTO> getFileList(Integer userId, Integer parentId, UserFileVO userFileVO) {
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
        var userFileDTOList = userFileMapper.getFileListByPage(page, userFileDO, userFileVO.getCategory(), suffixSet, userFileVO.getSortingField(), userFileVO.getSortingMethod());

        // 判断结果是否为空
        if (CollUtil.isNotEmpty(userFileDTOList)) {
            page.setRecords(userFileDTOList);
            return new PageResult<>(page);
        }

        return null;
    }

    @Override
//    public UserFileDO getUserFileByName(String fileName, Integer userId, Integer parentId, UserFileStatusEnum userFileStatus, UserFileItemTypeEnum itemType) {
    public UserFileDO getUserFileByName(String fileName, Integer userId, Integer parentId, UserFileStatusEnum userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
//        return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getName, fileName).eq(UserFileDO::getFileStatus, userFileStatus.flag()).eq(UserFileDO::getItemType, itemType.flag()).one();
        return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getName, fileName).eq(UserFileDO::getFileStatus, userFileStatus.flag()).one();
    }

    @Override
    public Integer newFolder(String folderName, Integer parentId, Integer userId) {
        return addUserFile(userId, null, parentId, folderName, UserFileItemTypeEnum.FOLDER, NORMAL, null);
    }

    @Override
    public UserFileDO getUserFileByIdAndUserId(Integer id, Integer userId, UserFileStatusEnum userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getId, id);
        if (userFileStatus != null) {
            lambdaQuery.eq(UserFileDO::getFileStatus, userFileStatus.flag());
        }
        return lambdaQuery.one();
    }

    @Override
    public Boolean updateFileNameById(Integer id, Integer userId, String newName, UserFileStatusEnum userFileStatus) {
        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFileDO::getName, newName).eq(UserFileDO::getId, id).eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatus.flag());
        var updateNameResult = userFileMapper.update(null, lambdaUpdateWrapper);

        return updateNameResult == 1;
    }

    @Override
    public Boolean rename(Integer id, Integer userId, String newName, UserFileDO userFileDO, UserFileStatusEnum userFileStatus) {
        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFileDO::getName, newName).eq(UserFileDO::getId, id).eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatus.flag());

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
    public UploadResultDTO secondUploadFile(Integer userId, Integer parentId, UploadFileVO uploadFileVO, FileDO fileDO) {
        // 插入数据库
        addUserFile(userId, fileDO.getId(), parentId, uploadFileVO.getFileName(), FILE, NORMAL, fileDO.getSize());
        return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_SECOND.id());
    }

    @Tran
    @Override
    public UploadResultDTO splitUploadFile(UploadedFile file, Integer userId, Integer parentId, UploadFileVO uploadFileVO, UploadFileCacheDTO uploadFileCacheDTO) throws IOException {
        UploadFileTempDTO uploadTempFileDTO;

        // 设置暂存临时目录
        var tempFolder = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, userId, uploadFileVO.getTaskId());
        var tempDir = FileUtil.file(tempFolder);
        uploadFileCacheDTO.setTempDir(tempDir);

        // 如果是第一片
        if (uploadFileVO.getChunkIndex() == 0) {
            if (file.getContentSize() > uploadFileVO.getFileSize()) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_FILE_SIZE_OVERFLOW);
            }

            // 增加上传中文件大小（整个文件大小）
            userRedisDAO.incrementUploadingFileSize(userId, uploadFileVO.getFileSize());

            // 切片是0，则表示redis中还没有数据，要新增
            var uploadFileTempDTO = BeanUtil.toBean(uploadFileVO, UploadFileTempDTO.class);
            uploadFileTempDTO.setUploadedSize(0L);
            uploadFileTempDTO.setTempFolder(tempFolder);
            uploadFileTempDTO.setParentId(parentId);
            fileRedisDAO.putUploadTempFileInfo(userId, uploadFileVO.getTaskId(), uploadFileTempDTO);

            // 如果文件夹不存在则创建目录
            if (!FileUtil.isDirectory(tempDir)) {
                if (!FileUtil.exist(tempDir)) {
                    FileUtil.mkdir(tempDir);
                }
            }
        } else {
            // 获取缓存中数据
//                var uploadTempFileDTO = fileRedisDAO.getUploadTempFileInfo(userId, uploadFileVO.getTaskId());
//
//                // 如果缓存中没有数据，则返回上传失败
//                if (uploadTempFileDTO == null) {
//                    // 删除缓存数据
//                    fileRedisDAO.delUploadTempFileInfo(userId, uploadFileVO.getTaskId());
//                    userRedisDAO.decrementUploadingFileSize(userId, uploadFileVO.getFileSize());
//                    FileUtil.del(tempDir);
//
//                    return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_FAIL.id());
//                }
//
//                // 如果缓存中有数据，但是实际上传文件大小已超过初始文件大小
//                if (uploadTempFileDTO.getFileSize() < (uploadTempFileDTO.getUploadedSize() + file.getSize())) {
//                    // 删除缓存数据
//                    fileRedisDAO.delUploadTempFileInfo(userId, uploadFileVO.getTaskId());
//                    userRedisDAO.decrementUploadingFileSize(userId, uploadFileVO.getFileSize());
//                    FileUtil.del(tempDir);
//
//                    return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_FAIL.id());
//                }

            // 不是第一片，增加uploadedSize
            fileRedisDAO.updateUploadedSize(userId, uploadFileVO.getTaskId(), file.getContentSize());
        }

        // 将文件写入临时目录
        File tempFile = FileUtil.file(tempDir, uploadFileVO.getChunkIndex().toString());
        file.transferTo(tempFile);

        // 如果不是最后一片，直接返回上传中
        if (uploadFileVO.getChunkIndex() < uploadFileVO.getTotalChunks() - 1) {
            return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOADING.id());
        }

        // 获取缓存中数据
        uploadTempFileDTO = fileRedisDAO.getUploadTempFileInfo(userId, uploadFileVO.getTaskId());

        // 如果是最后一片，执行合并分片操作
        var finalFilePath = fileService.mergeFile(uploadTempFileDTO.getFileName(), uploadFileVO.getTaskId(), tempFolder);
        var finalFullFilePath = FileUtils.generatePath(rootPath, finalFilePath);
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

        String thumbnailFileName = null;
//        String thumbnailFileName = DateUtil.format(new Date(), "yyyy/MM/dd") + StrUtil.SLASH + FileUtils.replaceFileExtName(finalFileName, SystemConsts.THUMBNAIL_SUFFIX);
        // 设置文件存储全路径
        var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL, thumbnailFileName);
        uploadFileCacheDTO.setThumbnailFilePath(thumbnailFilePath);

        // 图片生成缩略图
        if (CategoryEnum.isPictureByName(uploadTempFileDTO.getFileName())) {
//            var result = ImageUtils.generateThumbnail(finalFilePath, thumbnailFilePath, 150, -1);
//            thumbnailFileName = result ? thumbnailFileName : null;
        } else if (CategoryEnum.isVideoByName(uploadTempFileDTO.getFileName())) { // 视频生成缩略图
//            var result = VideoUtils.generateThumbnail(finalFilePath, thumbnailFilePath, 150);
//            thumbnailFileName = result ? thumbnailFileName : null;
        } else {
            thumbnailFileName = null;
        }

        if (thumbnailFileName == null) {
            uploadFileCacheDTO.setThumbnailFilePath(null);
        }

        // 写入File库，获取文件ID
        var fileId = fileService.addFile(finalFileName, finalFilePath, finalFileSize, uploadTempFileDTO.getIdentifier(), thumbnailFileName);

        if (fileId == null) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 插入数据库
        addUserFile(userId, fileId, parentId, uploadFileVO.getFileName(), FILE, NORMAL, finalFileSize);
        // 删除缓存数据
        fileRedisDAO.delUploadTempFileInfo(userId, uploadFileVO.getTaskId());
        userRedisDAO.decrementUploadingFileSize(userId, uploadTempFileDTO.getFileSize());
        FileUtil.del(tempDir);

        return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_FINISH.id());
    }

    @Override
    public void cancelUploadFile(Integer userId, String taskId) {

        var uploadTempFileInfo = fileRedisDAO.getUploadTempFileInfo(userId, taskId);

        if (uploadTempFileInfo == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_CANCEL_UPLOAD);
        }
        // 删除缓存数据
        fileRedisDAO.delUploadTempFileInfo(userId, taskId);
        userRedisDAO.decrementUploadingFileSize(userId, uploadTempFileInfo.getFileSize());
        var tempFolder = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, userId, taskId);
        var tempDir = FileUtil.file(tempFolder);
        FileUtil.del(tempDir);
    }

    @Override
    public void clearUploadFileCache(Integer userId, String taskId, Long fileSize, UploadFileCacheDTO uploadFileCacheDTO) {
        // 删除缓存数据
        fileRedisDAO.delUploadTempFileInfo(userId, taskId);
        userRedisDAO.decrementUploadingFileSize(userId, fileSize);
        FileUtil.del(uploadFileCacheDTO.getTempDir());
        FileUtil.del(uploadFileCacheDTO.getFinalFilePath());
        FileUtil.del(uploadFileCacheDTO.getThumbnailFilePath());
    }

    @Override
    public PageResult<UserFileDO> getFolderList(Integer userId, Integer parentId, UserFolderVO userFolderVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        var userFolderListPage = lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag()).eq(UserFileDO::getFileStatus, NORMAL.flag()).eq(UserFileDO::getParentId, parentId).orderByDesc(UserFileDO::getUpdateTime).page(new Page<>(userFolderVO.getPageNum(), userFolderVO.getPageSize()));

        // 判断结果是否为空
        if (CollUtil.isNotEmpty(userFolderListPage.getRecords())) {
            return new PageResult<>(userFolderListPage);
        }

        return null;
    }

    @Override
    public List<UserFileDO> getUserFileByIdsAndUserId(List<Integer> ids, Integer userId, UserFileStatusEnum userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        return lambdaQuery.eq(UserFileDO::getUserId, userId).in(UserFileDO::getId, ids).eq(UserFileDO::getFileStatus, userFileStatus.flag()).list();
    }

    @Override
    public List<Integer> getAllSubfolders(Integer userId, List<Integer> ids) {

        var totalSubfolderIds = new ArrayList<Integer>();

        var userFolderVO = new UserFolderVO();
        userFolderVO.setPageNum(1);
        // 小于0 表示不分页
        userFolderVO.setPageSize(-1);
        for (var id : ids) {
            var subfoldersPage = getFolderList(userId, id, userFolderVO);

            if (subfoldersPage == null || CollUtil.isEmpty(subfoldersPage.getList())) {
                continue;
            }

            var tempSubfolderIds = new ArrayList<Integer>();
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
    public Long getCountByNames(List<String> fileNames, Integer userId, Integer parentId, UserFileStatusEnum userFileStatus, UserFileItemTypeEnum itemType) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId).in(UserFileDO::getName, fileNames).eq(UserFileDO::getFileStatus, userFileStatus.flag()).eq(UserFileDO::getItemType, itemType.flag()).count();
    }

    @Tran
    @Override
    public void updateParentIdByIds(List<Integer> sourceIds, Integer targetId, Integer userId, UserFileStatusEnum userFileStatus) {
        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFileDO::getParentId, targetId).in(UserFileDO::getId, sourceIds).eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatus.flag());
        var updateNameCount = userFileMapper.update(null, lambdaUpdateWrapper);

        if (updateNameCount != sourceIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    @Override
    public List<UserFileDTO> getUserFileDTOListByIds(List<Integer> ids, Integer userId, UserFileStatusEnum userFileStatus) {
        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(userFileStatus.flag());
        var userFileDTOList = userFileMapper.getUserFileDTOByIds(userFileDO, ids);
        if (CollUtil.isEmpty(userFileDTOList)) {
            return null;
        }

        return userFileDTOList;
    }

    @Override
    public List<UserFileTreeDTO> getUserFileTreeListByIds(List<Integer> ids, Integer userId, UserFileStatusEnum userFileStatus) {
        var userFileDTOList = getUserFileDTOListByIds(ids, userId, userFileStatus);
        if (CollUtil.isEmpty(userFileDTOList)) {
            return null;
        }

        return BeanUtil.copyToList(userFileDTOList, UserFileTreeDTO.class);
    }

    @Override
    public void getSubUserFileTree(Integer userId, List<UserFileTreeDTO> userFileTreeList) {
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
    public void copy(Integer targetId, Integer userId, List<UserFileTreeDTO> sourceUserFileTreeDTOList, Long totalSize) {
        if (totalSize > 0) {
            // 增加上传中文件大小（整个文件大小）
            userRedisDAO.incrementUploadingFileSize(userId, totalSize);
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
            userRedisDAO.decrementUploadingFileSize(userId, totalSize);
        }
    }

    @Tran
    @Override
    public void delete(List<UserFileTreeDTO> delUserFileTreeList, Integer userId) {
        var delIds = new ArrayList<Integer>();
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
    public void updateUserFileStatusById(List<Integer> ids, Integer userId, UserFileStatusEnum userFileStatus) {
        var lambdaUpdateWrapper = new LambdaUpdateWrapper<UserFileDO>();
        lambdaUpdateWrapper.set(UserFileDO::getFileStatus, userFileStatus.flag()).eq(UserFileDO::getUserId, userId).in(UserFileDO::getId, ids);
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
     */
    @Tran
    private Integer addUserFile(Integer userId, Integer fileId, Integer parentId, String fileName, UserFileItemTypeEnum userFileItemType, UserFileStatusEnum userFileStatusEnum, Long fileSize) {

        var userFileDO = new UserFileDO();

        userFileDO.setUserId(userId);
        userFileDO.setItemType(userFileItemType.flag());
        userFileDO.setFileStatus(userFileStatusEnum.flag());
        userFileDO.setParentId(parentId);

        // 根据文件名称获取文件
//        var sameNameUserFile = getUserFileByName(fileName, userId, userFileDO.getParentId(), userFileStatusEnum, userFileItemType);
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

        return userFileDO.getId();
    }

    /**
     * 新增UserFileTree结构对象
     *
     * @param userFileTreeList 要插入的元素集合
     * @param userId           用户ID
     * @param parentId         父文件夹ID
     */
    @Tran
    private void insertUserFileTree(List<UserFileTreeDTO> userFileTreeList, Integer userId, Integer parentId) {
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
            if (UserFileItemTypeEnum.isFile(userFileTree.getItemType()) || CollUtil.isEmpty(userFileTree.getChildUserFileDTOList())) {
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
    private void getDelInfo(List<UserFileTreeDTO> userFileTreeList, List<Integer> delIds, List<RecycleBinDO> recycleBinList, Integer userId, String recycleId, Integer root) {
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

            if (UserFileItemTypeEnum.isFile(userFileTree.getItemType()) || CollUtil.isEmpty(userFileTree.getChildUserFileDTOList())) {
                continue;
            }

            // 递归删除子节点对象
            getDelInfo(userFileTree.getChildUserFileDTOList(), delIds, recycleBinList, userId, tempRecycleId, 0);
        }
    }


    @Override
    public List<UserFileDO> getUserFileListByUserIdAndParentId(Integer userId, Integer parentId, Integer userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getFileStatus, userFileStatus).list();
    }

    @Override
    public UserFileDO getParentUserFileByPathAndItemType(Integer userId, String path) {
        if (StrUtil.isEmpty(path)) {
            return null;
        }

        String[] dirs = path.split("/");

        if (dirs.length == 0) {
            return null;
        }

        var parentId = 0;
        for (int i = 1; i < dirs.length; i++) {
            var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
            var userFileDO = lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getName, dirs[i]).eq(UserFileDO::getFileStatus, NORMAL.flag()).eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag()).one();
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
    public List<UserFileTreeDTO> getUserFileTreeDTOByIdsAndUserId(List<Integer> ids, Integer userId, Integer userFileStatus) {
        var userFileDO = new UserFileDO();
        userFileDO.setUserId(userId);
        userFileDO.setFileStatus(userFileStatus);
        return userFileMapper.getUserFileTreeDTOByIdsAndUserId(userFileDO, ids);
    }

    @Override
    public void recursiveGetUserFileTreeDTO(List<UserFileTreeDTO> sourceUserFileTreeDTOList, Integer userId) {
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
    private List<UserFileTreeDTO> recursiveGetChildrenFile(UserFileTreeDTO userFileTreeDTO, Integer userId) {

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

//    @Override
//    public Integer updateFilePathByParentId(Integer parentId, Integer userId, String newPath, UserFileStatusEnum userFileStatusEnum) {
//        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
//        lambdaUpdateWrapper.set(UserFileDO::getPath, newPath).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatusEnum.flag());
//        return userFileMapper.update(null, lambdaUpdateWrapper);
//    }

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
    public UserFileDO getUserFileById(Integer id, Integer userId, Integer userFileStatus) {
//        try {
//            var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
//            return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getItemType, itemType).eq(UserFileDO::getName, name).count();
//
//            return userFileMapper.  .getUserFileById(id, userId, userFileStatus);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

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
    public List<UserFileDO> getUserFileByLikeFilePath(String filePath, Long userId, Integer userFileStatus) {
//        try {
//            return userFileDao.getUserFileByLikeFilePath(filePath, userId, userFileStatus);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        return null;
    }


    @Override
    public DownloadedFile download(List<UserFileTreeDTO> userFileTreeDTOList, Integer userId) throws IOException {
        if (userFileTreeDTOList.size() == 1 && UserFileItemTypeEnum.isFile(userFileTreeDTOList.getFirst().getItemType())) {
            return new DownloadedFile(new File(FileUtils.generatePath(rootPath, userFileTreeDTOList.getFirst().getPath())), userFileTreeDTOList.getFirst().getName());
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
    private void zipFiles(List<UserFileTreeDTO> userFileTreeDTOList, ZipOutputStream zipOutputStream, String parentPath, Boolean isRoot) {
        for (var userFileTreeDTO : userFileTreeDTOList) {
            var fileFullPath = parentPath == null ? userFileTreeDTO.getName() : FileUtils.generatePath(parentPath, userFileTreeDTO.getName());

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
                    try (FileInputStream fileInputStream = new FileInputStream(FileUtils.generatePath(rootPath, userFileTreeDTO.getPath()))) {

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
