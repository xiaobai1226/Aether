package com.xiaobai1226.aether.core.application;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
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
import com.xiaobai1226.aether.core.domain.dto.UploadFileCacheDTO;
import com.xiaobai1226.aether.core.domain.dto.UploadResultDTO;
import com.xiaobai1226.aether.core.domain.dto.UserFolderDTO;
import com.xiaobai1226.aether.core.domain.vo.*;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.service.intf.QuotaService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.core.usecase.file.*;
import com.xiaobai1226.aether.core.usecase.storage.SetFolderStorageSourceUseCase;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
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
    private DeleteFileUseCase deleteFileUseCase;

    @Inject
    private SetFolderStorageSourceUseCase setFolderStorageSourceUseCase;

    @Inject
    private RenameFileUseCase renameFileUseCase;

    @Inject
    private CreateFolderUseCase createFolderUseCase;

    @Inject
    private DownloadFileUseCase downloadFileUseCase;

    @Inject("${project.path.root}")
    private String rootPath;

    /**
     * 分页获取文件列表（查询型：统一入口，避免 Controller 直调 Service）
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
     * 上传文件
     * 
     * @param uploadFileVO 上传文件VO
     * @param file         上传文件
     * @param userId       用户ID
     * @return 上传结果
     */
    public UploadResultDTO uploadFile(UploadFileVO uploadFileVO, UploadedFile file, Long userId) {
        // 获取父文件夹对象（包含存储源信息）
        UserFolderDTO parentFolder = userFileService.getFolderDTO(userId, uploadFileVO.getPath());
        if (parentFolder == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        // 如果是上传文件夹，判断文件路径
        if (StrUtil.isNotEmpty(uploadFileVO.getRelativePath())) {
            var relativePath = uploadFileVO.getRelativePath();
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

        // 如果taskId为空，则生成taskId
        if (StrUtil.isBlank(uploadFileVO.getTaskId())) {
            String task = userId + uploadFileVO.getIdentifier() + DateUtil.format(new Date(), "yyyyMMddHHmmssSSS")
                    + RandomUtil.randomString(6);
            uploadFileVO.setTaskId(task);
        }

        // 如果是第一片文件，尝试秒传
        if (uploadFileVO.getChunkIndex() == 0) {
            var storageFileDO = userFileService.trySecondUpload(userId, parentFolder, uploadFileVO);
            if (storageFileDO != null) {
                return userFileService.secondUploadFile(userId, parentFolder, uploadFileVO, storageFileDO);
            }
        }

        var uploadFileCacheDTO = new UploadFileCacheDTO();
        try {
            return userFileService.splitUploadFile(file, userId, parentFolder, uploadFileVO, uploadFileCacheDTO);
        } catch (FailResultException e) {
            userFileService.clearUploadFileCache(userId, uploadFileVO.getTaskId(), uploadFileVO.getFileSize(),
                    uploadFileCacheDTO);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            userFileService.clearUploadFileCache(userId, uploadFileVO.getTaskId(), uploadFileVO.getFileSize(),
                    uploadFileCacheDTO);
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 取消上传文件
     * 
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    public void cancelUploadFile(String taskId, Long userId) {
        userFileService.cancelUploadFile(userId, taskId);
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
            final var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL,
                    thumbnail);
            if (!FileUtil.exist(thumbnailFilePath)) {
                // 保持原逻辑：文件不存在时，目前没有明确返回策略
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var file = FileUtil.file(thumbnailFilePath);
            var downloadedFile = new DownloadedFile(file);
            downloadedFile.asAttachment(false);
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

            var fileFullPath = FileUtils.generatePath(rootPath, fileDO.getPath());
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
                storageSource = storageSourceService.getDefaultStorageSource(userId);
                if (storageSource == null) {
                    throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
                }
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
                storageSource = storageSourceService.getDefaultStorageSource(userId);
                if (storageSource == null) {
                    throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
                }
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
     * WebDAV：按路径删除（进入回收站）
     */
    public boolean deleteByPath(String reqPath, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath)) {
                return false;
            }
            var userFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
            if (userFileDTO == null) {
                return false;
            }

            var fileTreeList = new ArrayList<UserFileTreeDTO>();
            var fileTree = new UserFileTreeDTO();
            fileTree.setId(userFileDTO.getId());
            fileTree.setItemType(userFileDTO.getItemType());
            fileTreeList.add(fileTree);

            if (UserFileItemTypeEnum.isFolder(userFileDTO.getItemType())) {
                userFileService.getSubUserFileTree(userId, fileTreeList);
            }

            userFileService.delete(fileTreeList, userId);
            return true;
        } catch (Exception e) {
            log.error("WebDAV按路径删除失败: reqPath={}", reqPath, e);
            return false;
        }
    }

    /**
     * WebDAV：按路径复制
     */
    public boolean copyByPath(String reqPath, String descPath, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath) || StrUtil.isEmpty(descPath)) {
                return false;
            }

            var sourceFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
            if (sourceFileDTO == null) {
                return false;
            }

            int lastSlashIndex = descPath.lastIndexOf("/");
            String targetParentPath = "";
            if (lastSlashIndex > 0) {
                targetParentPath = descPath.substring(0, lastSlashIndex);
            }

            Long targetParentId = 0L;
            UserFolderDTO targetParentFolder = null;
            if (StrUtil.isNotEmpty(targetParentPath)) {
                // 使用 getFolderDTO 获取目标父文件夹（包含存储源信息）
                targetParentFolder = userFileService.getFolderDTO(userId, targetParentPath);
                if (targetParentFolder == null) {
                    return false;
                }
                targetParentId = targetParentFolder.getId();
            }

            var sourceTreeList = new ArrayList<UserFileTreeDTO>();
            var sourceTree = new UserFileTreeDTO();
            sourceTree.setId(sourceFileDTO.getId());
            sourceTree.setItemType(sourceFileDTO.getItemType());
            sourceTree.setName(sourceFileDTO.getName());
            sourceTree.setParentId(sourceFileDTO.getParentId());
            sourceTreeList.add(sourceTree);

            if (UserFileItemTypeEnum.isFolder(sourceFileDTO.getItemType())) {
                userFileService.getSubUserFileTree(userId, sourceTreeList);
            }

            Long totalSize = userFileService.getUserFileTreeSpaceUsage(sourceTreeList);
            userFileService.copy(targetParentFolder, userId, sourceTreeList, totalSize);
            return true;
        } catch (Exception e) {
            log.error("WebDAV按路径复制失败: reqPath={}, descPath={}", reqPath, descPath, e);
            return false;
        }
    }

    /**
     * WebDAV：按路径移动（只更新UserFile结构；如需迁移存储源，后续统一走 move 用例）
     */
    public boolean moveByPath(String reqPath, String descPath, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath) || StrUtil.isEmpty(descPath)) {
                return false;
            }

            var sourceFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
            if (sourceFileDTO == null) {
                return false;
            }

            int lastSlashIndex = descPath.lastIndexOf("/");
            String targetParentPath = "";
            String targetName = descPath;
            if (lastSlashIndex > 0) {
                targetParentPath = descPath.substring(0, lastSlashIndex);
                targetName = descPath.substring(lastSlashIndex + 1);
            } else if (lastSlashIndex == 0) {
                targetName = descPath.substring(1);
            }

            Long targetParentId = 0L;
            if (StrUtil.isNotEmpty(targetParentPath)) {
                // 使用 getFolderDTO 获取目标父文件夹（包含存储源信息）
                var targetParentFolder = userFileService.getFolderDTO(userId, targetParentPath);
                if (targetParentFolder == null) {
                    return false;
                }
                targetParentId = targetParentFolder.getId();
            }

            boolean needRename = !sourceFileDTO.getName().equals(targetName);
            var sourceIds = new ArrayList<Long>();
            sourceIds.add(sourceFileDTO.getId());
            userFileService.updateParentIdByIds(sourceIds, targetParentId, userId, NORMAL);
            if (needRename) {
                userFileService.updateFileNameById(sourceFileDTO.getId(), userId, targetName, NORMAL);
            }

            return true;
        } catch (Exception e) {
            log.error("WebDAV按路径移动失败: reqPath={}, descPath={}", reqPath, descPath, e);
            return false;
        }
    }

    /**
     * WebDAV：按路径创建目录
     */
    public boolean mkdirByPath(String reqPath, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath)) {
                return false;
            }

            int lastSlashIndex = reqPath.lastIndexOf("/");
            String parentPath = "";
            String folderName = reqPath;
            if (lastSlashIndex > 0) {
                parentPath = reqPath.substring(0, lastSlashIndex);
                folderName = reqPath.substring(lastSlashIndex + 1);
            } else if (lastSlashIndex == 0) {
                folderName = reqPath.substring(1);
            }

            // 使用 UserFolderDTO 保持存储源信息，避免向下转型丢失信息
            UserFolderDTO parentFolder = null;
            Long parentId = 0L;
            if (StrUtil.isNotEmpty(parentPath)) {
                // 使用 getFolderDTO 获取父文件夹（包含存储源信息）
                parentFolder = userFileService.getFolderDTO(userId, parentPath);
                if (parentFolder == null) {
                    return false;
                }
                parentId = parentFolder.getId();
            }

            var existingFolder = userFileService.getUserFileByName(folderName, userId, parentId, NORMAL);
            if (existingFolder != null) {
                return false;
            }

            // 复用已有 newFolder 逻辑（保持一致的存储源继承策略）
            userFileService.newFolder(folderName, parentFolder, userId);
            return true;
        } catch (Exception e) {
            log.error("WebDAV按路径创建目录失败: reqPath={}", reqPath, e);
            return false;
        }
    }

    /**
     * WebDAV：写入文件（覆盖语义）
     *
     * 说明：WebDAV 并不是 Multipart 上传，因此这里通过写临时文件 + 计算 MD5 的方式复用主上传逻辑。
     */
    public boolean putFileByPath(String reqPath, InputStream in, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath) || in == null) {
                return false;
            }

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

            // 获取父目录（使用 UserFolderDTO 保持存储源信息）
            UserFolderDTO parentFolder = null;
            Long parentId = 0L;
            if (StrUtil.isNotEmpty(parentPath)) {
                // 使用 getFolderDTO 获取父文件夹（包含存储源信息）
                parentFolder = userFileService.getFolderDTO(userId, parentPath);
                if (parentFolder == null) {
                    return false;
                }
                parentId = parentFolder.getId();
            }

            // 覆盖：若同名存在，先删除到回收站（保持与现有删除语义一致）
            var existing = userFileService.getUserFileByName(fileName, userId, parentId, NORMAL);
            if (existing != null) {
                if (UserFileItemTypeEnum.isFolder(existing.getItemType())) {
                    // 同名目录无法覆盖
                    return false;
                }
                var tree = new UserFileTreeDTO();
                tree.setId(existing.getId());
                tree.setItemType(existing.getItemType());
                userFileService.delete(new ArrayList<>(List.of(tree)), userId);
            }

            // 写临时文件并计算 MD5
            String tempFileName = "webdav_" + RandomUtil.randomString(12);
            String tempFolder = FileUtils.generatePath(rootPath, "temp", "webdav", userId.toString());
            FileUtil.mkdir(tempFolder);
            File tempFile = FileUtil.file(tempFolder, tempFileName);

            var md5 = MessageDigest.getInstance("MD5");
            try (OutputStream out = Files.newOutputStream(tempFile.toPath())) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    md5.update(buffer, 0, len);
                    out.write(buffer, 0, len);
                }
            }

            String identifier = bytesToHex(md5.digest());
            userFileService.uploadWholeFile(tempFile, userId, parentFolder, fileName, identifier);
            return true;
        } catch (Exception e) {
            log.error("WebDAV putFile 失败: reqPath={}", reqPath, e);
            return false;
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