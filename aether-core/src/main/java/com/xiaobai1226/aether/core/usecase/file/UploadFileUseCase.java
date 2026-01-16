package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.cache.FileCache;
import com.xiaobai1226.aether.core.domain.dto.*;
import com.xiaobai1226.aether.core.domain.vo.DeleteVO;
import com.xiaobai1226.aether.core.domain.vo.UploadFileVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.enums.UserFileStatusEnum;
import com.xiaobai1226.aether.core.infrastructure.storage.StorageBackendFactory;
import com.xiaobai1226.aether.core.service.impl.StorageMigrationService;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.service.intf.QuotaService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.support.ThumbnailService;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import com.xiaobai1226.aether.dao.domain.entity.StorageSourceDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.data.annotation.Tran;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.*;
import static com.xiaobai1226.aether.core.enums.UploadStatusEnum.*;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 上传文件用例
 * 
 * 负责处理所有文件上传相关的业务逻辑，包括：
 * - 秒传（文件已存在时直接引用）
 * - 分片上传
 * - 整文件上传（WebDAV等场景）
 * - 取消上传
 * 
 * @author bai
 */
@Component
@Slf4j
public class UploadFileUseCase {

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private FileCache fileCache;

    @Inject
    private QuotaService quotaService;

    @Inject
    private FileService fileService;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageBackendFactory storageBackendFactory;

    @Inject
    private StorageMigrationService storageMigrationService;

    @Inject
    private UserFileService userFileService;

    @Inject
    private ThumbnailService thumbnailService;

    @Inject
    private DeleteFileUseCase deleteFileUseCase;

    /**
     * 秒传文件（当文件已存在时使用）
     *
     * @param userId       用户ID
     * @param parentFolder 父文件夹对象
     * @param uploadFileVO 上传文件相关信息
     * @param fileDO       文件信息
     * @return 上传结果
     */
    public UploadResultDTO secondUploadFile(final Long userId, UserFolderDTO parentFolder, UploadFileVO uploadFileVO,
            FileDO fileDO) {
        // 插入数据库
        var userFile = addUserFile(userId, fileDO.getId(), parentFolder.getId(), uploadFileVO.getFileName(), FILE,
                NORMAL,
                fileDO.getSize(),
                parentFolder.getStorageSourceId());

        // 检查是否需要异步迁移存储源
        if (!Objects.equals(fileDO.getStorageSourceId(), parentFolder.getStorageSourceId())) {
            // 文件的存储源与目标文件夹的存储源不一致，标记为待迁移
            storageMigrationService.markForMigration(userFile.getId(), parentFolder.getStorageSourceId(), userId);
            log.info("秒传文件需要迁移存储源: userFileId={}, sourceStorageId={}, targetStorageId={}",
                    userFile.getId(), fileDO.getStorageSourceId(), parentFolder.getStorageSourceId());
        }

        return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_SECOND.id());
    }

    /**
     * 尝试秒传文件（检查文件是否已存在，如果存在则直接秒传或复制）
     *
     * @param userId         用户ID
     * @param parentUserFile 父文件夹对象
     * @param uploadFileVO   上传文件信息
     * @return 上传结果，如果返回null表示无法秒传，需要继续正常上传流程
     */
    public FileDO trySecondUpload(final Long userId, UserFolderDTO parentUserFile, UploadFileVO uploadFileVO) {
        // TODO 检测存储空间是否足够
        // var userSpaceUsage = userService.getUserSpaceUsage(userId);
        // if (userSpaceUsage == null || userSpaceUsage.getRealRemainStorage() <
        // uploadFileVO.getFileSize()) {
        // throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
        // }

        // 检测文件是否已存在（获取所有相同identifier的文件列表）
        List<FileDO> existingFileList = fileService.getFileListByIdentifier(uploadFileVO.getIdentifier());

        // 若文件列表为空，返回null表示无法秒传
        if (CollUtil.isEmpty(existingFileList)) {
            return null;
        }

        // 查找是否有同存储源的文件
        FileDO storageFileDO = existingFileList.stream()
                .filter(f -> f.getStorageSourceId() != null
                        && f.getStorageSourceId().equals(parentUserFile.getStorageSourceId()))
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

            return storageFileDO;
        }

        // 情况3：有文件但没有同存储源的，直接使用已存在的文件，异步迁移存储源
        FileDO sourceFileDO = existingFileList.get(0); // 选择第一个作为源文件

        // TODO 如果前端传过来的文件大小，小于数据库中记录的，则重新判断空间是否足够
        // if (uploadFileVO.getFileSize() < sourceFileDO.getSize()) {
        // if (userSpaceUsage.getRealRemainStorage() < sourceFileDO.getSize()) {
        // throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
        // }
        // }

        // 需要执行秒传操作，返回已存在的文件（后续会标记异步迁移）
        return sourceFileDO;
    }

    /**
     * 分片上传文件
     *
     * @param file               上传的文件
     * @param userId             用户ID
     * @param parentFolder       父文件夹对象
     * @param uploadFileVO       上传文件相关信息
     * @param uploadFileCacheDTO 上传文件缓存相关信息
     * @return 上传结果
     */
    @Tran
    public UploadResultDTO splitUploadFile(UploadedFile file, final Long userId, UserFolderDTO parentFolder,
            UploadFileVO uploadFileVO, UploadFileCacheDTO uploadFileCacheDTO) throws IOException {
        UploadFileTempDTO uploadTempFileDTO;

        // 设置暂存临时目录（使用配置的rootPath）
        var tempFolder = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, userId.toString(),
                uploadFileVO.getTaskId());
        var tempDir = FileUtil.file(tempFolder);
        uploadFileCacheDTO.setTempDir(tempDir);

        // long parentId = parentUserFile != null ? parentUserFile.getId() : 0L;

        // 如果是第一片
        if (uploadFileVO.getChunkIndex() == 0) {
            if (file.getContentSize() > uploadFileVO.getFileSize()) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_FILE_SIZE_OVERFLOW);
            }

            // TODO 预占上传空间（整个文件大小）
            // quotaService.reserveUploading(userId, uploadFileVO.getFileSize());

            // 切片是0，则表示redis中还没有数据，要新增
            var uploadFileTempDTO = BeanUtil.toBean(uploadFileVO, UploadFileTempDTO.class);
            uploadFileTempDTO.setUploadedSize(0L);
            uploadFileTempDTO.setTempFolder(tempFolder);
            uploadFileTempDTO.setParentId(parentFolder.getId());
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

        // 如果是最后一片，执行合并分片操作（文件存储到存储源）
        var finalFilePath = fileService.mergeFile(uploadTempFileDTO.getFileName(), uploadFileVO.getTaskId(), tempFolder,
                parentFolder.getStorageSource().getPath());
        var finalFullFilePath = FileUtils.generatePath(parentFolder.getStorageSource().getPath(), finalFilePath);
        uploadFileCacheDTO.setFinalFilePath(finalFilePath);

        // 获取最终文件
        var finalFile = FileUtil.file(finalFullFilePath);
        // 获取最终文件大小
        var finalFileSize = FileUtil.size(finalFile);
        var finalFileName = finalFile.getName();

        // TODO 如果最终文件大小，大于初始文件大小
        // if (finalFileSize > uploadTempFileDTO.getFileSize()) {
        // // 重新检测文件大小是否足够
        // var userSpaceUsage = userService.getUserSpaceUsage(userId);
        // // 如果空间不足则返回上传失败
        // if (userSpaceUsage.getRealRemainStorage() < (finalFileSize -
        // uploadTempFileDTO.getFileSize())) {
        // throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
        // }
        // }

        // 生成缩略图
        var thumbnailResult = thumbnailService.generateThumbnail(finalFullFilePath, finalFileName, finalFileSize);
        String thumbnailFileName = thumbnailResult.isSuccess() ? thumbnailResult.getThumbnailFileName() : null;
        uploadFileCacheDTO.setThumbnailFilePath(thumbnailResult.getThumbnailFilePath());

        // 写入File库，获取文件ID（storageSourceId已在前面获取）
        var fileDO = fileService.addFile(finalFileName, finalFilePath, finalFileSize, uploadTempFileDTO.getIdentifier(),
                thumbnailFileName, parentFolder.getStorageSourceId());

        if (fileDO == null) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 插入数据库
        addUserFile(userId, fileDO.getId(), parentFolder.getId(), uploadFileVO.getFileName(), FILE, NORMAL,
                finalFileSize, parentFolder.getStorageSourceId());
        // 删除缓存数据
        fileCache.delUploadTempFileInfo(userId, uploadFileVO.getTaskId());
        // TODO 释放上传预占空间
        // quotaService.releaseUploading(userId, uploadTempFileDTO.getFileSize());
        FileUtil.del(tempDir);

        return new UploadResultDTO(uploadFileVO.getTaskId(), UPLOAD_FINISH.id());
    }

    /**
     * 上传整文件（用于 WebDAV 等非 Multipart 场景）
     *
     * <p>
     * 说明：该方法会复用现有的存储源选择、缩略图生成、FileDO/UserFile 记录落库与空间统计规则。
     * WebDAV 场景下会自动覆盖同名文件。
     * </p>
     *
     * @param localFile      本地临时文件
     * @param userId         用户ID
     * @param parentUserFile 父目录（可为 null 表示根目录）
     * @param fileName       用户侧展示名称
     * @param identifier     内容标识（例如 MD5）
     */
    @Tran
    public UploadResultDTO uploadWholeFile(File localFile, final Long userId, UserFolderDTO parentUserFile,
            String fileName, String identifier) throws IOException {
        if (localFile == null || !localFile.exists() || localFile.isDirectory()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }
        if (StrUtil.isBlank(fileName)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NAME_EMPTY);
        }
        if (StrUtil.isBlank(identifier)) {
            throw new FailResultException(PARAM_IS_INVALID);
        }

        long fileSize = localFile.length();
        // TODO 预占上传空间
        // quotaService.reserveUploading(userId, fileSize);

        // 选择存储源（优化：如果 parentUserFile 是 UserFolderDTO，直接使用其存储源信息）
        var storageSource = getStorageSourceByParent(parentUserFile, userId);
        if (storageSource == null) {
            // quotaService.releaseUploading(userId, fileSize);
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
        }
        Long storageSourceId = storageSource.getId();

        // 生成任务ID与存储文件名
        String taskId = IdUtil.simpleUUID();
        var storedFileName = FileUtils.rename(fileName, taskId);
        var relativePath = FileUtils.generatePath(FolderNameConsts.PATH_UPLOAD_FILE_FULL,
                DateUtil.format(new Date(), "yyyy/MM/dd"), storedFileName);

        // 先落盘（轻量一致性套路：先落物理对象，再落库；失败时清理物理对象）
        var backend = storageBackendFactory.getByType(0);
        try {
            backend.putFile(storageSource.getPath(), relativePath, localFile, true);

            // 生成缩略图（仍使用 rootPath 统一存储）
            String thumbnailToStore = null;
            String absPath = backend.tryResolveAbsolutePath(storageSource.getPath(), relativePath);
            if (absPath != null) {
                // var thumbnailResult = thumbnailService.generateThumbnail(absPath, storedFileName, fileSize);
                // thumbnailToStore = thumbnailResult.isSuccess() ? thumbnailResult.getThumbnailFileName() : null;
            }

            // FileDO
            var fileDO = fileService.addFile(storedFileName, relativePath, fileSize, identifier, thumbnailToStore,
                    storageSourceId);
            if (fileDO == null) {
                throw new FailResultException(SYSTEM_ERROR);
            }

            long parentId = parentUserFile != null ? parentUserFile.getId() : 0L;

            // 检查是否存在同名文件（WebDAV 场景需要覆盖）
            var existing = userFileService.getUserFileByName(fileName, userId, parentId, NORMAL);
            if (existing != null) {
                if (UserFileItemTypeEnum.isFolder(existing.getItemType())) {
                    // 同名目录无法覆盖
                    throw new FailResultException(BAD_REQUEST_ERROR, ERROR_FILE_NO_EXIST);
                }
                // WebDAV PUT 覆盖：旧文件进入回收站（与 DELETE 操作保持一致，用户可恢复）
                DeleteVO deleteVO = new DeleteVO();
                deleteVO.setIds(List.of(existing.getId()));
                deleteFileUseCase.execute(deleteVO.getIds(), userId);
                log.info("WebDAV 覆盖上传：旧文件已移入回收站 userFileId={}", existing.getId());
            }

            // 创建新文件记录（使用原有的 addUserFile 方法，不修改其逻辑）
            // 由于旧文件已删除，addUserFile 检测不到重名，会直接使用原文件名
            addUserFile(userId, fileDO.getId(), parentId, fileName, FILE, NORMAL, fileSize, storageSourceId);

            // 释放上传预占（已用空间由 addUserFile 内统一增加）
            // quotaService.releaseUploading(userId, fileSize);
            return new UploadResultDTO(taskId, UPLOAD_FINISH.id());
        } catch (

        Exception e) {
            // 清理：释放预占 + 删除已落盘文件（尽量）
            // quotaService.releaseUploading(userId, fileSize);
            try {
                backend.delete(storageSource.getPath(), relativePath);
            } catch (Exception ignore) {
            }
            if (e instanceof FailResultException fre) {
                throw fre;
            }
            throw new FailResultException(SYSTEM_ERROR);
        } finally {
            // 清理临时文件（WebDAV场景）
            try {
                Files.deleteIfExists(localFile.toPath());
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 取消上传
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     */
    public void cancelUploadFile(final Long userId, String taskId) {

        var uploadTempFileInfo = fileCache.getUploadTempFileInfo(userId, taskId);

        if (uploadTempFileInfo == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_CANCEL_UPLOAD);
        }
        // 删除缓存数据
        fileCache.delUploadTempFileInfo(userId, taskId);
        quotaService.releaseUploading(userId, uploadTempFileInfo.getFileSize());
        var tempFolder = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, userId, taskId);
        var tempDir = FileUtil.file(tempFolder);
        FileUtil.del(tempDir);
    }

    /**
     * 清除缓存数据
     *
     * @param userId             用户ID
     * @param taskId             任务ID
     * @param fileSize           文件大小
     * @param uploadFileCacheDTO 上传文件缓存相关信息
     */
    public void clearUploadFileCache(final Long userId, String taskId, Long fileSize,
            UploadFileCacheDTO uploadFileCacheDTO) {
        // 删除缓存数据
        fileCache.delUploadTempFileInfo(userId, taskId);
        quotaService.releaseUploading(userId, fileSize);
        FileUtil.del(uploadFileCacheDTO.getTempDir());
        FileUtil.del(uploadFileCacheDTO.getFinalFilePath());
        FileUtil.del(uploadFileCacheDTO.getThumbnailFilePath());
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
        // 新建文件和文件夹默认使用继承类型
        userFileDO.setStorageSourceType(1);

        // 根据文件名称获取文件
        var sameNameUserFile = userFileService.getUserFileByName(fileName, userId, userFileDO.getParentId(),
                userFileStatusEnum);
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

        var resultCount = userFileService.save(userFileDO);

        if (!resultCount) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        if (UserFileItemTypeEnum.isFile(userFileItemType)) {
            // 更新用户已使用存储空间（集中到 QuotaService，避免多流程漏改）
            quotaService.increaseUsed(userId, fileSize == null ? 0L : fileSize);
        }

        return userFileDO;
    }

    /**
     * 根据父文件夹获取存储源对象
     * 优化：如果传入的是 UserFolderDTO 且已包含存储源信息，直接返回，避免重复查询
     * 
     * @param parentUserFile 父文件夹
     * @param userId         用户ID
     * @return 存储源对象
     */
    private StorageSourceDO getStorageSourceByParent(UserFileDO parentUserFile, Long userId) {
        // 优化：如果传入的是 UserFolderDTO 且已包含存储源信息，直接返回，避免重复查询
        if (parentUserFile instanceof UserFolderDTO folder && folder.getStorageSource() != null) {
            return folder.getStorageSource();
        }

        // 否则通过 ID 查询
        Long storageSourceId = userFileService.getStorageSourceIdByParent(parentUserFile, userId);
        if (storageSourceId == null) {
            return null;
        }

        return storageSourceService.getStorageSourceById(storageSourceId, userId);
    }
}