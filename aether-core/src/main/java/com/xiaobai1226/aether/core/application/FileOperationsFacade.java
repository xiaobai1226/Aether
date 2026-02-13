package com.xiaobai1226.aether.core.application;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.enums.CategoryEnum;
import com.xiaobai1226.aether.common.enums.FileTypeEnum;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.common.util.ImageUtils;
import com.xiaobai1226.aether.core.cache.DownloadCache;
import com.xiaobai1226.aether.core.domain.dto.UploadChunkResultDTO;
import com.xiaobai1226.aether.core.domain.dto.UploadResultDTO;
import com.xiaobai1226.aether.core.domain.dto.UploadTaskInitDTO;
import com.xiaobai1226.aether.core.domain.dto.UploadTaskStatusDTO;
import com.xiaobai1226.aether.core.domain.dto.UserFolderDTO;
import com.xiaobai1226.aether.core.domain.vo.*;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.service.intf.QuotaService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.core.service.support.ThumbnailService;
import com.xiaobai1226.aether.core.usecase.file.*;
import com.xiaobai1226.aether.core.usecase.storage.SetFolderStorageSourceUseCase;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.http.ContentType.OCTET_STREAM;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 文件操作门面（Facade，门面模式）
 * 目标：把复杂流程统一收敛到一个入口，供 HTTP Controller 与 WebDAV 复用，降低重复实现与漂移风险。
 * 说明：这是“渐进式重构”的第一步，内部仍复用现有 Service 的实现，后续会逐步拆分为更细粒度的用例类/领域服务。
 */
@Component
@Slf4j
public class FileOperationsFacade {

    @Inject
    private UserFileService userFileService;

    @Inject
    private UserService userService;

    @Inject
    private QuotaService quotaService;

    @Inject
    private FileService fileService;

    @Inject
    private DownloadCache downloadCache;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private MoveFileUseCase moveFileUseCase;

    @Inject
    private CopyFileUseCase copyFileUseCase;

    @Inject
    private CopyAndRenameUseCase copyAndRenameUseCase;

    @Inject
    private DeleteFileUseCase deleteFileUseCase;

    @Inject
    private SetFolderStorageSourceUseCase setFolderStorageSourceUseCase;

    @Inject
    private RenameFileUseCase renameFileUseCase;

    @Inject
    private CreateFolderUseCase createFolderUseCase;

    @Inject
    private DownloadFileUseCase downloadFileUseCase;

    @Inject
    private UploadFileUseCase uploadFileUseCase;

    @Inject
    private ThumbnailService thumbnailService;

    @Inject("${project.path.root}")
    private String rootPath;

    /**
     * 分页获取文件列表
     */
    public PageResult<UserFileDTO> getFileListByPage(UserFileVO userFileVO, Long userId) {
        var parentId = 0L;
        if (userFileVO.getCategory() == null && StrUtil.isNotEmpty(userFileVO.getPath())) {
            // 使用 getFolderDTO 简化 path 到 parentId 的转换
            var folder = userFileService.getFolderDTO(userId, userFileVO.getPath());
            if (folder == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_PARENT_FOLDER_NO_EXIST);
            }
            parentId = folder.getId();
        }

        if (userFileVO.getSortingField() == null) {
            userFileVO.setSortingField(0);
        }
        if (userFileVO.getSortingMethod() == null) {
            userFileVO.setSortingMethod(0);
        }
        return userFileService.getFileList(userId, parentId, userFileVO);
    }

    /**
     * 分页获取文件夹列表（查询型：统一入口，避免 Controller 直调 Service）
     */
    public PageResult<UserFileDO> getFolderListByPage(UserFolderVO userFolderVO, Long userId) {
        // 使用 getFolderDTO 简化 path 到 parentId 的转换
        var folder = userFileService.getFolderDTO(userId, userFolderVO.getPath());

        if (folder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        return userFileService.getFolderList(userId, folder.getId(), userFolderVO);
    }

    /**
     * 初始化上传任务
     */
    public UploadTaskInitDTO uploadInit(UploadInitVO uploadInitVO, Long userId) {
        // 获取父文件夹对象（包含存储源信息）
        UserFolderDTO parentFolder = userFileService.getFolderDTO(userId, uploadInitVO.getPath());
        if (parentFolder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        // 如果是上传文件夹，判断文件路径
        if (StrUtil.isNotEmpty(uploadInitVO.getRelativePath())) {
            var relativePath = uploadInitVO.getRelativePath();
            int lastIndex = relativePath.lastIndexOf("/");
            if (lastIndex > 0) {
                relativePath = relativePath.substring(0, lastIndex);
            }

            var newParentUserFileDO = userFileService.getParentFolderByPathOrCreate(userId, parentFolder, relativePath);
            if (newParentUserFileDO == null) {
                throw new FailResultException(SYSTEM_ERROR);
            }
            var newParentFolder = BeanUtil.copyProperties(newParentUserFileDO, UserFolderDTO.class);
            newParentFolder.setStorageSourceId(parentFolder.getStorageSourceId());
            newParentFolder.setStorageSource(parentFolder.getStorageSource()); // 继承父目录的存储源对象
            parentFolder = newParentFolder;
        }

        return uploadFileUseCase.initUploadTask(userId, parentFolder, uploadInitVO);
    }

    /**
     * 上传单个切片
     */
    public UploadChunkResultDTO uploadChunk(UploadChunkVO uploadChunkVO, UploadedFile file,
            Long userId) {
        try {
            return uploadFileUseCase.uploadChunk(file, userId, uploadChunkVO);
        } catch (FailResultException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 查询上传状态
     */
    public UploadTaskStatusDTO uploadStatus(UploadStatusVO uploadStatusVO, Long userId) {
        return uploadFileUseCase.getUploadTaskStatus(userId, uploadStatusVO.getTaskId());
    }

    /**
     * 完成上传任务
     */
    public UploadResultDTO uploadComplete(UploadCompleteVO uploadCompleteVO, Long userId) {
        UserFolderDTO parentFolder = userFileService.getFolderDTO(userId, uploadCompleteVO.getPath());
        if (parentFolder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }
        return uploadFileUseCase.completeUploadTask(userId, parentFolder, uploadCompleteVO);
    }

    /**
     * 取消上传任务
     */
    public void uploadCancel(UploadCancelVO uploadCancelVO, Long userId) {
        uploadFileUseCase.cancelUploadTask(userId, uploadCancelVO.getTaskId());
    }

    /**
     * 创建文件夹
     * 
     * @param newFolderVO 创建文件夹VO
     * @param userId      用户ID
     */
    public void newFolder(NewFolderVO newFolderVO, Long userId) {
        // 调用UseCase执行业务逻辑
        createFolderUseCase.execute(newFolderVO.getFolderName(), newFolderVO.getPath(), userId);
    }

    /**
     * 重命名文件
     * 
     * @param fileRenameVO 重命名文件VO
     * @param userId       用户ID
     */
    public void rename(FileRenameVO fileRenameVO, Long userId) {
        // 调用UseCase执行业务逻辑
        renameFileUseCase.execute(fileRenameVO.getId(), fileRenameVO.getNewName(), userId);
    }

    /**
     * 移动文件
     * 
     * @param moveVO 移动文件VO
     * @param userId 用户ID
     */
    public void move(MoveVO moveVO, Long userId) {
        var targetFolder = userFileService.getFolderDTO(userId, moveVO.getTargetPath());
        if (targetFolder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_TARGET_FOLDER_NO_EXIST);
        }

        // 调用UseCase执行业务逻辑，传递 targetFolder 避免重复查询存储源
        moveFileUseCase.execute(moveVO.getSourceIds(), targetFolder, userId);
    }

    /**
     * 复制文件
     * 
     * @param copyVO 复制文件VO
     * @param userId 用户ID
     */
    public void copy(CopyVO copyVO, Long userId) {
        var targetFolder = userFileService.getFolderDTO(userId, copyVO.getTargetPath());
        if (targetFolder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_TARGET_FOLDER_NO_EXIST);
        }

        // 调用UseCase执行业务逻辑，传递 targetFolder 避免重复查询
        copyFileUseCase.execute(copyVO.getSourceIds(), targetFolder, userId);
    }

    /**
     * 复制并重命名文件/文件夹
     * 
     * @param copyAndRenameVO 复制并重命名VO
     * @param userId          用户ID
     */
    public void copyAndRename(CopyAndRenameVO copyAndRenameVO, Long userId) {
        var targetFolder = userFileService.getFolderDTO(userId, copyAndRenameVO.getTargetPath());
        if (targetFolder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_TARGET_FOLDER_NO_EXIST);
        }

        // 调用 UseCase 执行业务逻辑
        copyAndRenameUseCase.execute(
                copyAndRenameVO.getSourceId(),
                targetFolder,
                copyAndRenameVO.getNewName(),
                userId);
    }

    /**
     * 删除文件到回收站
     * 
     * @param deleteVO 删除文件VO
     * @param userId   用户ID
     */
    public void deleteToRecycle(DeleteVO deleteVO, Long userId) {
        // 调用UseCase执行业务逻辑
        deleteFileUseCase.execute(deleteVO.getIds(), userId);
    }

    /**
     * 创建下载链接
     * 
     * @param ids    文件ID列表（逗号分隔）
     * @param userId 用户ID
     * @return 下载签名
     */
    public String createDownloadSign(String ids, Long userId) {
        // 1. 参数转换
        List<Long> idList = Arrays.stream(ids.split(StrUtil.COMMA))
                .mapToLong(Long::parseLong)
                .boxed()
                .collect(Collectors.toList());

        // 2. 调用UseCase创建签名
        return downloadFileUseCase.createDownloadSign(idList, userId);
    }

    /**
     * 下载文件
     * 
     * @param sign 下载签名
     * @return 下载文件
     */
    public DownloadedFile downloadBySign(String sign) {
        try {
            // 调用UseCase执行下载
            return downloadFileUseCase.downloadBySign(sign);
        } catch (IOException e) {
            log.error("下载文件失败", e);
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 获取缩略图
     * 
     * @param thumbnail 缩略图路径
     * @return 缩略图文件
     */
    public DownloadedFile getThumbnail(String thumbnail) {
        try {
            var downloadedFile = thumbnailService.getThumbnailFile(thumbnail);
            if (downloadedFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }
            return downloadedFile;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 获取图片
     * 
     * @param id     文件ID
     * @param userId 用户ID
     * @return 图片文件
     */
    public DownloadedFile getImage(Long id, Long userId) {
        try {
            var userFileDO = userFileService.getUserFileByIdAndUserId(id, userId, NORMAL);
            if (userFileDO == null || !CategoryEnum.isPictureBySuffix(userFileDO.getSuffix())) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var fileDO = fileService.getFileById(userFileDO.getFileId());
            if (fileDO == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var storageSource = storageSourceService.getStorageSourceById(fileDO.getStorageSourceId(), userId);
            if (storageSource == null) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
            }

            var fileFullPath = FileUtils.generatePath(storageSource.getPath(), fileDO.getPath());
            if (!FileUtil.exist(fileFullPath)) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            DownloadedFile downloadedFile;
            if (FileTypeEnum.isHeic(fileDO.getSuffix())) {
                var webpBytes = ImageUtils.heic2Webp(fileFullPath);
                if (webpBytes == null) {
                    throw new FailResultException(SYSTEM_ERROR);
                }
                downloadedFile = new DownloadedFile(OCTET_STREAM.getValue(), webpBytes, userFileDO.getName());
            } else {
                var file = FileUtil.file(fileFullPath);
                downloadedFile = new DownloadedFile(file, userFileDO.getName());
            }

            downloadedFile.asAttachment(false);
            return downloadedFile;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 获取视频
     * 
     * @param id     文件ID
     * @param userId 用户ID
     * @return 视频文件
     */
    public DownloadedFile getVideo(Long id, Long userId) {
        try {
            var userFileDO = userFileService.getUserFileByIdAndUserId(id, userId, NORMAL);
            if (userFileDO == null || !CategoryEnum.isVideoBySuffix(userFileDO.getSuffix())) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var fileDO = fileService.getFileById(userFileDO.getFileId());
            if (fileDO == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var storageSource = storageSourceService.getStorageSourceById(fileDO.getStorageSourceId(), userId);
            if (storageSource == null) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
            }

            var fileFullPath = FileUtils.generatePath(storageSource.getPath(), fileDO.getPath());
            if (!FileUtil.exist(fileFullPath)) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var file = FileUtil.file(fileFullPath);
            var downloadedFile = new DownloadedFile(file, userFileDO.getName());
            downloadedFile.asAttachment(false);
            return downloadedFile;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 获取文件
     * 
     * @param id     文件ID
     * @param userId 用户ID
     * @return 文件文件
     */
    public DownloadedFile getFile(Long id, Long userId) {
        try {
            var userFileDO = userFileService.getUserFileByIdAndUserId(id, userId, NORMAL);
            if (userFileDO == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var fileDO = fileService.getFileById(userFileDO.getFileId());
            if (fileDO == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var storageSource = storageSourceService.getStorageSourceById(fileDO.getStorageSourceId(), userId);
            if (storageSource == null) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
            }

            var fileFullPath = FileUtils.generatePath(storageSource.getPath(), fileDO.getPath());
            if (!FileUtil.exist(fileFullPath)) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var file = FileUtil.file(fileFullPath);
            var downloadedFile = new DownloadedFile(file, userFileDO.getName());
            downloadedFile.asAttachment(false);
            return downloadedFile;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 设置文件夹存储源
     * 
     * @param setFolderStorageSourceVO 设置文件夹存储源VO
     * @param userId                   用户ID
     */
    public void setFolderStorageSource(SetFolderStorageSourceVO setFolderStorageSourceVO, Long userId) {
        // 调用UseCase执行业务逻辑（异步迁移，用户无感知）
        setFolderStorageSourceUseCase.execute(
                setFolderStorageSourceVO.getFolderId(),
                setFolderStorageSourceVO.getStorageSourceId(),
                userId);
    }

    /**
     * WebDAV：写入文件（覆盖语义）
     *
     * 说明：WebDAV 并不是 Multipart 上传，因此这里通过写临时文件 + 计算 MD5 的方式复用主上传逻辑。
     */
    public boolean putFileByPath(String reqPath, InputStream in, Long userId) {
        File tempFile = null;
        long startTime = System.currentTimeMillis();
        try {
            if (StrUtil.isEmpty(reqPath) || in == null) {
                log.warn("putFileByPath 参数无效: reqPath={}, userId={}", reqPath, userId);
                return false;
            }

            log.info("putFileByPath 开始: reqPath={}, userId={}", reqPath, userId);

            // 解析路径，获取父目录和文件名
            int lastSlashIndex = reqPath.lastIndexOf("/");
            String parentPath = "";
            String fileName = reqPath;
            if (lastSlashIndex > 0) {
                parentPath = reqPath.substring(0, lastSlashIndex);
                fileName = reqPath.substring(lastSlashIndex + 1);
            } else if (lastSlashIndex == 0) {
                fileName = reqPath.substring(1);
            }

            log.debug("路径解析: parentPath={}, fileName={}", parentPath, fileName);

            // 获取父目录（使用 UserFolderDTO 保持存储源信息）
            // 注意：parentPath 为空代表根目录，也需要获取 UserFolderDTO（包含默认存储源）
            UserFolderDTO parentFolder = userFileService.getFolderDTO(userId, parentPath);
            if (parentFolder == null) {
                log.warn("父文件夹不存在: parentPath={}, userId={}", parentPath, userId);
                return false;
            }

            // 写临时文件并计算 MD5（使用与 HTTP 上传相同的临时目录结构）
            String tempFileName = "webdav_" + RandomUtil.randomString(12);
            String tempFolder = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, "webdav",
                    userId.toString());
            FileUtil.mkdir(tempFolder);
            tempFile = FileUtil.file(tempFolder, tempFileName);

            log.debug("开始写入临时文件: {}", tempFile.getPath());
            long fileSize = 0;
            var md5 = MessageDigest.getInstance("MD5");
            try (OutputStream out = Files.newOutputStream(tempFile.toPath())) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    md5.update(buffer, 0, len);
                    out.write(buffer, 0, len);
                    fileSize += len;
                }
            }

            String identifier = bytesToHex(md5.digest());
            log.debug("临时文件写入完成: size={} bytes, md5={}", fileSize, identifier);
            
            // 尝试秒传（复用已存在的相同内容文件，节省存储空间和数据库记录）
            // 构造 UploadFileVO 用于秒传检查
            var uploadFileVO = new UploadFileVO();
            uploadFileVO.setFileName(fileName);
            uploadFileVO.setFileSize(fileSize);
            uploadFileVO.setIdentifier(identifier);
            uploadFileVO.setTaskId("webdav_" + System.currentTimeMillis());
            
            var existingFileDO = uploadFileUseCase.trySecondUpload(userId, parentFolder, uploadFileVO);
            if (existingFileDO != null) {
                log.info("WebDAV 上传触发秒传: reqPath={}, identifier={}, userId={}", reqPath, identifier, userId);
                
                // 检查是否存在同名文件（WebDAV PUT 语义需要覆盖）
                var existing = userFileService.getUserFileByName(fileName, userId, parentFolder.getId(), NORMAL);
                if (existing != null) {
                    if (UserFileItemTypeEnum.isFolder(existing.getItemType())) {
                        log.warn("WebDAV 秒传失败：同名目录已存在 fileName={}, userId={}", fileName, userId);
                        return false;
                    }
                    // 旧文件进入回收站（与 DELETE 操作保持一致，用户可恢复）
                    DeleteVO deleteVO = new DeleteVO();
                    deleteVO.setIds(List.of(existing.getId()));
                    deleteFileUseCase.execute(deleteVO.getIds(), userId);
                    log.info("WebDAV 秒传覆盖：旧文件已移入回收站 userFileId={}", existing.getId());
                }
                
                // 执行秒传（创建新的 UserFileDO 记录，引用已存在的物理文件）
                uploadFileUseCase.secondUploadFile(userId, parentFolder, uploadFileVO, existingFileDO);
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("WebDAV 秒传成功: reqPath={}, size={} bytes, duration={}ms, userId={}", 
                        reqPath, fileSize, duration, userId);
                return true;
            }
            
            log.debug("WebDAV 无法秒传，继续正常上传流程: identifier={}", identifier);
            
            // 无法秒传，调用 uploadWholeFile 正常上传（内部会自动处理同名文件覆盖逻辑）
            uploadFileUseCase.uploadWholeFile(tempFile, userId, parentFolder, fileName, identifier);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("putFileByPath 成功: reqPath={}, size={} bytes, duration={}ms, userId={}", 
                    reqPath, fileSize, duration, userId);
            return true;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("putFileByPath 失败: reqPath={}, duration={}ms, userId={}", reqPath, duration, userId, e);
            return false;
        } finally {
            // 清理临时文件，防止磁盘空间泄漏
            if (tempFile != null && tempFile.exists()) {
                try {
                    FileUtil.del(tempFile);
                    log.debug("WebDAV 临时文件已清理: {}", tempFile.getPath());
                } catch (Exception e) {
                    log.warn("WebDAV 临时文件清理失败: {}", tempFile.getPath(), e);
                }
            }
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }
}