package com.xiaobai1226.aether.core.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.core.constant.FolderNameConsts;
import com.xiaobai1226.aether.core.dao.redis.DownloadRedisDAO;
import com.xiaobai1226.aether.core.domain.dto.*;
import com.xiaobai1226.aether.core.domain.entity.UserFileDO;
import com.xiaobai1226.aether.core.domain.vo.*;
import com.xiaobai1226.aether.core.enums.UserFileCategoryEnum;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.core.util.FileUtils;
import com.xiaobai1226.aether.core.util.Result;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.validation.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static com.xiaobai1226.aether.core.constant.GateWayTagConsts.API_V1;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.core.enums.ResultCodeEnum.*;
import static com.xiaobai1226.aether.core.enums.ResultSuccessMsgEnum.*;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FOLDER;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 文件Controller
 *
 * @author bai
 */
@Component(tag = API_V1)
@Mapping("/file")
@Valid
public class FileController {

    @Inject
    private UserFileService userFileService;

    @Inject
    private UserService userService;

    @Inject
    private FileService fileService;

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private DownloadRedisDAO downloadRedisDAO;

    /**
     * 分页获取文件列表
     *
     * @param userFileVO 用户文件VO
     */
    @Get
    @Mapping("/getFileListByPage")
    public PageResultDataDTO<UserFileDTO> getFileListByPage(UserFileVO userFileVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        var parentId = 0;
        if (StrUtil.isNotEmpty(userFileVO.getPath())) {
            var parentUserFile = userFileService.getParentFolderByPath(userId, parentId, userFileVO.getPath());
            if (parentUserFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_PARENT_FOLDER_NO_EXIST);
            }
            parentId = parentUserFile.getId();
        }

        if (userFileVO.getSortField() == null) {
            userFileVO.setSortField(1);
        }
        if (userFileVO.getSortOrder() == null) {
            userFileVO.setSortOrder(1);
        }
        return userFileService.getFileList(userId, parentId, userFileVO);
    }

    /**
     * 新建文件夹
     *
     * @author bai
     */
    @Post
    @Mapping("/newFolder")
    public Result newFolder(@Validated NewFolderVO newFolderVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        var userId = StpUtil.getLoginIdAsInt();

        var parentId = 0;
        if (StrUtil.isNotEmpty(newFolderVO.getPath())) {
            var parentUserFile = userFileService.getParentFolderByPath(userId, parentId, newFolderVO.getPath());
            if (parentUserFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }
            parentId = parentUserFile.getId();
        }

        userFileService.newFolder(newFolderVO.getFolderName(), parentId, userId);

        return Result.success(SUCCESS_MSG_CREATE_FOLDER.msg());
    }

    /**
     * 重命名
     *
     * @author bai
     */
    @Post
    @Mapping("/rename")
    public Result rename(@Validated FileRenameVO fileRenameVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        var userId = StpUtil.getLoginIdAsInt();

        var userFileDO = userFileService.getUserFileByIdAndUserId(fileRenameVO.getId(), userId, NORMAL);

        if (userFileDO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

//        var existUserFileDO = userFileService.getUserFileByName(fileRenameVO.getNewName(), userId, userFileDO.getParentId(), NORMAL, UserFileItemTypeEnum.getEnumByFlag(userFileDO.getItemType()));
        var existUserFileDO = userFileService.getUserFileByName(fileRenameVO.getNewName(), userId, userFileDO.getParentId(), NORMAL);

        if (existUserFileDO != null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NAME_EXIST);
        }

        // 修改名称
        var result = userFileService.rename(fileRenameVO.getId(), userId, fileRenameVO.getNewName(), userFileDO, NORMAL);

        if (!result) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_RENAME);
        }

        return Result.success(SUCCESS_MSG_RENAME.msg());
    }

    /**
     * 上传文件
     *
     * @param uploadFileVO 上传文件参数
     * @param file         上传的文件
     * @return 上传结果
     * @author bai
     */
    @Post
    @Mapping(path = "/uploadFile")
    public UploadResultDTO uploadFile(@Validated UploadFileVO uploadFileVO, UploadedFile file) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        Integer parentId = 0;
        if (StrUtil.isNotEmpty(uploadFileVO.getPath())) {
            var parentUserFile = userFileService.getParentFolderByPath(userId, parentId, uploadFileVO.getPath());
            if (parentUserFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }
            parentId = parentUserFile.getId();
        }

        // 如果是上传文件夹，判断文件路径
        if (StrUtil.isNotEmpty(uploadFileVO.getRelativePath())) {
            var relativePath = uploadFileVO.getRelativePath();
            int lastIndex = relativePath.lastIndexOf("/");
            if (lastIndex > 0) {
                relativePath = relativePath.substring(0, lastIndex);
            }

            parentId = userFileService.getParentFolderByPathOrCreate(userId, parentId, relativePath);

            if (parentId == null) {
                throw new FailResultException(SYSTEM_ERROR);
            }
        }

        // 如果taskId为空，则生成taskId
        if (StrUtil.isBlank(uploadFileVO.getTaskId())) {
            // 生成taskId
            String task = userId + uploadFileVO.getIdentifier() + DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + RandomUtil.randomString(6);
            uploadFileVO.setTaskId(task);
        }

        // 如果是第一片文件
        if (uploadFileVO.getChunkIndex() == 0) {
            // 检测存储空间是否足够
            var userSpaceUsage = userService.getUserSpaceUsage(userId);
            if (userSpaceUsage == null || userSpaceUsage.getRealRemainStorage() < uploadFileVO.getFileSize()) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
            }

            // 检测文件是否已存在
            var fileDO = fileService.getFileByIdentifier(uploadFileVO.getIdentifier());
            // 若已存在则执行秒传操作
            if (fileDO != null) {
                // 如果前端传过来的文件大小，小于数据库中记录的，则重新判断空间是否足够
                if (uploadFileVO.getFileSize() < fileDO.getSize()) {
                    if (userSpaceUsage.getRealRemainStorage() < fileDO.getSize()) {
                        throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
                    }
                }

                // 执行秒传操作
                return userFileService.secondUploadFile(userId, parentId, uploadFileVO, fileDO);
            }
        }

        var uploadFileCacheDTO = new UploadFileCacheDTO();
        try {
            // 执行分片上传操作
            return userFileService.splitUploadFile(file, userId, parentId, uploadFileVO, uploadFileCacheDTO);
        } catch (FailResultException e) {
            // 报异常清理缓存
            userFileService.clearUploadFileCache(userId, uploadFileVO.getTaskId(), uploadFileVO.getFileSize(), uploadFileCacheDTO);

            throw e;
        } catch (Exception e) {
            // 报异常清理缓存
            userFileService.clearUploadFileCache(userId, uploadFileVO.getTaskId(), uploadFileVO.getFileSize(), uploadFileCacheDTO);

            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 取消上传文件
     *
     * @param taskId 上传任务ID
     * @author bai
     */
    @Post
    @Mapping(path = "/cancelUploadFile")
    public void cancelUploadFile(@Validated @NotNull(message = ERROR_TASK_ID_EMPTY) String taskId) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();
        userFileService.cancelUploadFile(userId, taskId);
    }

    /**
     * 分页获取用户文件夹方法
     */
    @Get
    @Mapping("/getFolderListByPage")
    public PageResultDataDTO<UserFileDO> getFolderListByPage(@Validated UserFolderVO userFolderVO) {
        // 获取当前会话账号id, 并转化为`long`类型
        var userId = StpUtil.getLoginIdAsInt();

        var parentId = 0;
        if (StrUtil.isNotEmpty(userFolderVO.getPath())) {
            var parentUserFile = userFileService.getParentFolderByPath(userId, parentId, userFolderVO.getPath());
            if (parentUserFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_PARENT_FOLDER_NO_EXIST);
            }
            parentId = parentUserFile.getId();
        }

        return userFileService.getFolderList(userId, parentId, userFolderVO);
    }

    /**
     * 移动
     *
     * @param moveVO 移动操作所需数据
     */
    @Post
    @Mapping("/move")
    public Result move(@Validated MoveVO moveVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        var userId = StpUtil.getLoginIdAsInt();

        if (moveVO == null || StrUtil.isEmpty(moveVO.getSourceIds())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_CONTENT_EMPTY);
        }

        List<Integer> sourceIds = Arrays.stream(moveVO.getSourceIds().split(StrUtil.COMMA)).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        if (CollUtil.isEmpty(sourceIds)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_CONTENT_EMPTY);
        }

        // 获取要移动的文件
        var sourceUserFileDOList = userFileService.getUserFileByIdsAndUserId(sourceIds, userId, NORMAL);

        if (CollUtil.isEmpty(sourceUserFileDOList) || sourceUserFileDOList.size() != sourceIds.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_CONTENT_EMPTY);
        }

        var targetId = 0;
        // 如果不是根目录则判断目标文件夹是否存在
        if (StrUtil.isNotEmpty(moveVO.getTargetPath())) {
            var targetUserFile = userFileService.getParentFolderByPath(userId, targetId, moveVO.getTargetPath());
            if (targetUserFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_TARGET_FOLDER_NO_EXIST);
            }
            targetId = targetUserFile.getId();
        }

        // 如果目标目录就是文件所在目录，直接返回
        if (sourceUserFileDOList.getFirst().getParentId() == targetId) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_IN_CURRENT_FOLDER);
        }

        // 要移动的文件名称集合
        var sourceFileNames = new ArrayList<String>();
        // 要移动的文件夹名称集合
        var sourceFolderNames = new ArrayList<String>();
        // 要移动的文件夹ID集合
        var sourceFolderIds = new ArrayList<Integer>();

        sourceUserFileDOList.forEach(sourceUserFileDO -> {
            if (UserFileItemTypeEnum.isFile(sourceUserFileDO.getItemType())) {
                sourceFileNames.add(sourceUserFileDO.getName());
            } else {
                sourceFolderIds.add(sourceUserFileDO.getId());
                sourceFolderNames.add(sourceUserFileDO.getName());
            }
        });

        // 校验，不能将文件移动到自身或其子目录下
        if (CollUtil.isNotEmpty(sourceFolderIds)) {
            if (sourceFolderIds.contains(targetId)) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_TARGET_IS_ITSELF_OR_SUB);
            }
            // 获取所有子文件夹ID
            var subfolderIds = userFileService.getAllSubfolders(userId, sourceFolderIds);
            if (CollUtil.isNotEmpty(subfolderIds) && subfolderIds.contains(targetId)) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_TARGET_IS_ITSELF_OR_SUB);
            }
        }

        // 校验目标文件夹下是否存在同名文件
        if (CollUtil.isNotEmpty(sourceFileNames)) {
            var existNameCount = userFileService.getCountByNames(sourceFileNames, userId, targetId, NORMAL, FILE);
            if (existNameCount > 0) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_TARGET_CONTAIN_SAME_NAME_FILE);
            }
        }
        // 校验目标文件夹下是否存在同名文件夹
        if (CollUtil.isNotEmpty(sourceFolderNames)) {
            var existNameCount = userFileService.getCountByNames(sourceFolderNames, userId, targetId, NORMAL, FOLDER);
            if (existNameCount > 0) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_MOVE_TARGET_CONTAIN_SAME_NAME_FOLDER);
            }
        }

        // 根据id集合修改parentId
        userFileService.updateParentIdByIds(sourceIds, targetId, userId, NORMAL);

        return Result.success(SUCCESS_MSG_MOVE.msg());
    }

    /**
     * 复制
     *
     * @param copyVO 移动操作所需数据
     * @author bai
     */
    @Post
    @Mapping("/copy")
    public Result copy(@Validated CopyVO copyVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        var userId = StpUtil.getLoginIdAsInt();

        if (copyVO == null || StrUtil.isEmpty(copyVO.getSourceIds())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_CONTENT_EMPTY);
        }

        List<Integer> sourceIds = Arrays.stream(copyVO.getSourceIds().split(StrUtil.COMMA)).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        if (CollUtil.isEmpty(sourceIds)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_CONTENT_EMPTY);
        }

        // 获取要复制的源文件
        var sourceUserFileTreeList = userFileService.getUserFileTreeListByIds(sourceIds, userId, NORMAL);
        if (CollUtil.isEmpty(sourceUserFileTreeList) || sourceUserFileTreeList.size() != sourceIds.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_CONTENT_EMPTY);
        }

        var targetId = 0;
        // 如果不是根目录则判断目标文件夹是否存在
        if (StrUtil.isNotEmpty(copyVO.getTargetPath())) {
            var targetUserFile = userFileService.getParentFolderByPath(userId, targetId, copyVO.getTargetPath());
            if (targetUserFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_TARGET_FOLDER_NO_EXIST);
            }
            targetId = targetUserFile.getId();
        }

        if (sourceUserFileTreeList.getFirst().getParentId() == targetId) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_IN_CURRENT_FOLDER);
        }

        // 要复制的文件名称集合
        var sourceFileNames = new ArrayList<String>();
        // 要复制的文件夹名称集合
        var sourceFolderNames = new ArrayList<String>();
        // 要复制的文件夹ID集合
        var sourceFolderIds = new ArrayList<Integer>();

        sourceUserFileTreeList.forEach(sourceUserFileDO -> {
            if (UserFileItemTypeEnum.isFile(sourceUserFileDO.getItemType())) {
                sourceFileNames.add(sourceUserFileDO.getName());
            } else {
                sourceFolderIds.add(sourceUserFileDO.getId());
                sourceFolderNames.add(sourceUserFileDO.getName());
            }
        });

        // 校验，不能将文件复制到自身或其子目录下
        if (CollUtil.isNotEmpty(sourceFolderIds)) {
            if (sourceFolderIds.contains(targetId)) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_IS_ITSELF_OR_SUB);
            }
            // 获取所有子文件夹ID
            var subfolderIds = userFileService.getAllSubfolders(userId, sourceFolderIds);
            if (CollUtil.isNotEmpty(subfolderIds) && subfolderIds.contains(targetId)) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_IS_ITSELF_OR_SUB);
            }
        }

        // 校验目标文件夹下是否存在同名文件
        if (CollUtil.isNotEmpty(sourceFileNames)) {
            var existNameCount = userFileService.getCountByNames(sourceFileNames, userId, targetId, NORMAL, FILE);
            if (existNameCount > 0) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_CONTAIN_SAME_NAME_FILE);
            }
        }
        // 校验目标文件夹下是否存在同名文件夹
        if (CollUtil.isNotEmpty(sourceFolderNames)) {
            var existNameCount = userFileService.getCountByNames(sourceFolderNames, userId, targetId, NORMAL, FOLDER);
            if (existNameCount > 0) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_COPY_TARGET_CONTAIN_SAME_NAME_FOLDER);
            }
        }

        // 获取全部要复制文件
        userFileService.getSubUserFileTree(userId, sourceUserFileTreeList);

        var totalSize = userFileService.getUserFileTreeSpaceUsage(sourceUserFileTreeList);

        // 检测存储空间是否足够
        var userSpaceUsage = userService.getUserSpaceUsage(userId);
        if (userSpaceUsage == null || userSpaceUsage.getRealRemainStorage() < totalSize) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
        }

        // 复制文件
        userFileService.copy(targetId, userId, sourceUserFileTreeList, totalSize);

        return Result.success(SUCCESS_MSG_COPY.msg());
    }

    /**
     * 删除文件或文件夹
     *
     * @author bai
     */
    @Post
    @Mapping("/delete")
    public Result delete(@Validated DeleteVO deleteVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        if (deleteVO == null || StrUtil.isEmpty(deleteVO.getIds())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DEL_CONTENT_EMPTY);
        }

        List<Integer> ids = Arrays.stream(deleteVO.getIds().split(StrUtil.COMMA)).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        if (CollUtil.isEmpty(ids)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DEL_CONTENT_EMPTY);
        }

        // 获取要删除的文件
        var delUserFileTreeList = userFileService.getUserFileTreeListByIds(ids, userId, NORMAL);

        if (CollUtil.isEmpty(delUserFileTreeList) || delUserFileTreeList.size() != ids.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DEL_CONTENT_EMPTY);
        }

        // 获取全部要删除的文件
        userFileService.getSubUserFileTree(userId, delUserFileTreeList);

        // 删除文件
        userFileService.delete(delUserFileTreeList, userId);

        return Result.success(SUCCESS_MSG_DELETE.msg());
    }

    /**
     * 获取缩略图
     */
    @Get
    @Mapping("/getThumbnail")
    public void getThumbnail(Context ctx, @Param("thumbnail") String thumbnail) {
        // 设置文件存储全路径
        final var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL, thumbnail);

        // 判断文件是否存在
        if (!FileUtil.exist(thumbnailFilePath)) {
            // TODO 这没写
        }

        ctx.contentType("image/jpg");
        FileUtils.readFile(ctx, thumbnailFilePath);
    }

    /**
     * 获取图片
     */
    @Get
    @Mapping("/getImage")
    public void getImage(Context ctx, @Param("id") Integer id) {
        // 获取当前会话账号id, 并转化为`int`类型
        var userId = StpUtil.getLoginIdAsInt();

        var userFileDO = userFileService.getUserFileByIdAndUserId(id, userId, NORMAL);

        if (userFileDO == null || !UserFileCategoryEnum.isPicture(userFileDO.getCategory())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        var fileDO = fileService.getFileById(userFileDO.getFileId());

        // 判断文件是否存在
        if (fileDO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        var fileFullPath = FileUtils.generatePath(rootPath, fileDO.getPath());

        if (!FileUtil.exist(fileFullPath)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        ctx.contentType("image/" + FileNameUtil.extName(fileDO.getName()));
        FileUtils.readFile(ctx, fileFullPath);
    }

    /**
     * 获取视频
     */
    @Get
    @Mapping("/getVideo")
    public void getVideo(Context ctx, @Param("id") Integer id) {
        // 获取当前会话账号id, 并转化为`int`类型
        var userId = StpUtil.getLoginIdAsInt();

        try {
            var userFileDO = userFileService.getUserFileByIdAndUserId(id, userId, NORMAL);

            if (userFileDO == null || !UserFileCategoryEnum.isVideo(userFileDO.getCategory())) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var fileDO = fileService.getFileById(userFileDO.getFileId());

            // 判断文件是否存在
            if (fileDO == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var fileFullPath = FileUtils.generatePath(rootPath, fileDO.getPath());

            if (!FileUtil.exist(fileFullPath)) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            var file = FileUtil.file(fileFullPath);

            var downloadedFile = new DownloadedFile(file, userFileDO.getName());

            //不做为附件下载（按需配置）
            downloadedFile.asAttachment(false);

            //也可用接口输出
            ctx.outputAsFile(downloadedFile);
        } catch (IOException e) {
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 获取文件
     */
    @Get
    @Mapping("/getFile")
    public void getFile(Context ctx, @Param("id") Integer id) {
        // 获取当前会话账号id, 并转化为`int`类型
        var userId = StpUtil.getLoginIdAsInt();

        var userFileDO = userFileService.getUserFileByIdAndUserId(id, userId, NORMAL);

        if (userFileDO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        var fileDO = fileService.getFileById(userFileDO.getFileId());

        // 判断文件是否存在
        if (fileDO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        var fileFullPath = FileUtils.generatePath(rootPath, fileDO.getPath());

        if (!FileUtil.exist(fileFullPath)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        FileUtils.readFile(ctx, fileFullPath);
    }

    /**
     * 创建下载链接
     *
     * @param ids 要下载的文件ID集合
     * @author bai
     */
    @Post
    @Mapping("/createDownloadSign")
    public String createDownloadSign(@Validated @NotBlank(message = ERROR_DOWNLOAD_CONTENT_EMPTY) String ids) {

        // 获取当前会话账号id, 并转化为`int`类型
        var userId = StpUtil.getLoginIdAsInt();

        List<Integer> idList = Arrays.stream(ids.split(StrUtil.COMMA)).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        if (CollUtil.isEmpty(idList)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DOWNLOAD_CONTENT_EMPTY);
        }

        var userFileDTOList = userFileService.getUserFileDTOListByIds(idList, userId, NORMAL);

        if (CollUtil.isEmpty(userFileDTOList) || userFileDTOList.size() != idList.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        String sign = RandomUtil.randomString(20);

        downloadRedisDAO.setDownloadSign(new DownloadFileDTO(idList, userId), sign);

        return sign;
    }

    /**
     * 下载
     *
     * @author bai
     */
    @Get
    @Mapping("/download")
    public void download(Context ctx, @Param("sign") String sign) {
        try {
            var downloadFileDTO = downloadRedisDAO.getDownloadInfo(sign);

            // 判断sign是否存在
            if (downloadFileDTO == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_SIGN);
            }

            var userFileTreeDTOList = userFileService.getUserFileTreeListByIds(downloadFileDTO.getIds(), downloadFileDTO.getUserId(), NORMAL);

            if (CollUtil.isEmpty(userFileTreeDTOList) || userFileTreeDTOList.size() != downloadFileDTO.getIds().size()) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            // 获取全部要下载的文件
            userFileService.getSubUserFileTree(downloadFileDTO.getUserId(), userFileTreeDTOList);

            var downloadedFile = userFileService.download(userFileTreeDTOList, downloadFileDTO.getUserId());

            //也可用接口输出
            ctx.outputAsFile(downloadedFile);
        } catch (IOException e) {
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

//    private void getImage2(HttpServletResponse response, String imageFolder, String imageName) {
//        if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName) || StringTools.pathIsOk(imageFolder) || !StringTools.pathIsOk(imageName)) {
//            return;
//        }
//        String imageSuffix = StringTools.getFileSuffix(imageName);
//        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
//        imageSuffix = imageSuffix.replace(".", "");
//        String contentType = "image/" + imageSuffix;
//        response.setContentType(contentType);
//        response.setHeader("Cache-Control", "max-age=2592000");
//        readFile(response, filePath);
//    }

    /**
     * 获取视频
     */
//    @GetMapping("/ts/getVideoInfo/{fileId}")
//    public void getVideo(HttpServletResponse response, @PathVariable("fileId") String fileId) {
//        getFile2(response, fileId, null);
//    }

    /**
     * 获取文件
     */
//    public void getFile2(HttpServletResponse response, String fileId, String userId) {
//        String filePath = null;
//
//        if (fileId.endsWith(".ts")) {
//            String[] tsArray = fileId.split("_");
//            String realFileId = tsArray[0];
//            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(realFileId, userId);
//            if (null == fileInfo) {
//                return;
//            }
//            String fileName = fileInfo.getFilePath();
//            fileName = StringTools.getFileNameNoSuffix(fileName) + "/" + fileId;
//            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileName;
//        } else {
//            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
//            if (null == fileInfo) {
//                return;
//            }
//            if (FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
//                String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
//                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileNameNoSuffix + "/" + Constants.M3U8_NAME;
//            } else {
//                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath();
//            }
//
//            File file = new File(filePath);
//            if (!file.exists()) {
//                return;
//            }
//        }
//
//        readFile(response, filePath);
//    }

    /**
     * 获取文件
     */
//    @GetMapping("/getFile/{fileId}")
//    public void getFile(HttpServletResponse response, @PathVariable("fileId") String fileId) {
//        getFile2(response, fileId, null);
//    }

    /**
     * 视频有根据文件ID获取面包屑导航的接口，视频中把所有文件信息都返回了，不合理，按自己的方式改造
     */
//    public void getFile() {
//        // TODO 待做，看视频
//    }
}