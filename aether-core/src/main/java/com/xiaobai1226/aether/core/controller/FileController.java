package com.xiaobai1226.aether.core.controller;

import com.xiaobai1226.aether.core.annotation.CurrentUserId;
import com.xiaobai1226.aether.core.application.FileOperationsFacade;
import com.xiaobai1226.aether.core.domain.dto.*;
import com.xiaobai1226.aether.core.domain.vo.*;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.common.domain.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.validation.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import static com.xiaobai1226.aether.common.constant.GateWayTagConsts.API_V1;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.*;
import static com.xiaobai1226.aether.common.enums.ResultSuccessMsgEnum.*;

/**
 * 文件Controller
 *
 * @author bai
 */
@Component(tag = API_V1)
@Mapping("/file")
@Valid
@Slf4j
public class FileController {

    @Inject
    private FileOperationsFacade fileOperationsFacade;

    /**
     * 分页获取文件列表
     *
     * @param userFileVO 用户文件VO
     */
    @Get
    @Mapping("/getFileListByPage")
    public PageResult<UserFileDTO> getFileListByPage(UserFileVO userFileVO, @CurrentUserId Long userId) {
        return fileOperationsFacade.getFileListByPage(userFileVO, userId);
    }

    /**
     * 新建文件夹
     *
     * @author bai
     */
    @Post
    @Mapping("/newFolder")
    public Result<Void> newFolder(@Validated NewFolderVO newFolderVO, @CurrentUserId Long userId) {
        fileOperationsFacade.newFolder(newFolderVO, userId);

        return Result.success(SUCCESS_MSG_CREATE_FOLDER.msg());
    }

    /**
     * 重命名
     *
     * @author bai
     */
    @Post
    @Mapping("/rename")
    public Result<Void> rename(@Validated FileRenameVO fileRenameVO, @CurrentUserId Long userId) {
        fileOperationsFacade.rename(fileRenameVO, userId);

        return Result.success(SUCCESS_MSG_RENAME.msg());
    }

    /**
     * 初始化上传任务
     */
    @Post
    @Mapping(path = "/uploadInit")
    public UploadTaskInitDTO uploadInit(@Validated UploadInitVO uploadInitVO, @CurrentUserId Long userId) {
        return fileOperationsFacade.uploadInit(uploadInitVO, userId);
    }

    /**
     * 上传单个切片
     */
    @Post
    @Mapping(path = "/uploadChunk")
    public UploadChunkResultDTO uploadChunk(@Validated UploadChunkVO uploadChunkVO, @Param("file") UploadedFile file,
            @CurrentUserId Long userId) {
        return fileOperationsFacade.uploadChunk(uploadChunkVO, file, userId);
    }

    /**
     * 查询上传任务状态
     */
    @Get
    @Mapping(path = "/uploadStatus")
    public UploadTaskStatusDTO uploadStatus(@Validated UploadStatusVO uploadStatusVO, @CurrentUserId Long userId) {
        return fileOperationsFacade.uploadStatus(uploadStatusVO, userId);
    }

    /**
     * 完成上传任务
     */
    @Post
    @Mapping(path = "/uploadComplete")
    public UploadResultDTO uploadComplete(@Validated UploadCompleteVO uploadCompleteVO, @CurrentUserId Long userId) {
        return fileOperationsFacade.uploadComplete(uploadCompleteVO, userId);
    }

    /**
     * 取消上传任务
     */
    @Post
    @Mapping(path = "/uploadCancel")
    public void uploadCancel(@Validated UploadCancelVO uploadCancelVO, @CurrentUserId Long userId) {
        fileOperationsFacade.uploadCancel(uploadCancelVO, userId);
    }

    /**
     * 分页获取用户文件夹方法
     */
    @Get
    @Mapping("/getFolderListByPage")
    public PageResult<UserFileDO> getFolderListByPage(@Validated UserFolderVO userFolderVO,
            @CurrentUserId Long userId) {
        return fileOperationsFacade.getFolderListByPage(userFolderVO, userId);
    }

    /**
     * 移动
     *
     * @param moveVO 移动操作所需数据
     */
    @Post
    @Mapping("/move")
    public Result<Void> move(@Validated MoveVO moveVO, @CurrentUserId Long userId) {
        fileOperationsFacade.move(moveVO, userId);

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
    public Result<Void> copy(@Validated CopyVO copyVO, @CurrentUserId Long userId) {
        fileOperationsFacade.copy(copyVO, userId);

        return Result.success(SUCCESS_MSG_COPY.msg());
    }

    /**
     * 删除文件或文件夹
     *
     * @author bai
     */
    @Post
    @Mapping("/delete")
    public Result<Void> delete(@Validated DeleteVO deleteVO, @CurrentUserId Long userId) {
        fileOperationsFacade.deleteToRecycle(deleteVO, userId);

        return Result.success(SUCCESS_MSG_DELETE.msg());
    }

    /**
     * 获取缩略图
     */
    @Get
    @Mapping("/getThumbnail")
    public void getThumbnail(Context ctx, @Param("thumbnail") String thumbnail) {
        try {
            var previewFile = fileOperationsFacade.getThumbnailLocalFile(thumbnail);
            if (previewFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }
            if (setCacheHeadersForPreview(ctx, previewFile.getFile(), 2592000)) {
                return;
            }
            ctx.outputAsFile(new DownloadedFile(previewFile.getFile(), previewFile.getFileName()));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 获取图片
     */
    @Get
    @Mapping("/getImage")
    public void getImage(Context ctx, @Param("id") Long id, @CurrentUserId Long userId) {
        try {
            var previewFile = fileOperationsFacade.getImageLocalFile(id, userId);
            if (previewFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }
            if (setCacheHeadersForPreview(ctx, previewFile.getFile(), 2592000)) {
                return;
            }
            ctx.outputAsFile(new DownloadedFile(previewFile.getFile(), previewFile.getFileName()));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 获取视频
     */
    @Get
    @Mapping("/getVideo")
    public void getVideo(Context ctx, @Param("id") Long id, @CurrentUserId Long userId) {
        try {
            var previewFile = fileOperationsFacade.getVideoLocalFile(id, userId);
            if (previewFile == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }
            outputFileWithRangeSupport(ctx, previewFile.getFile(), previewFile.getFileName());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 获取文件
     */
    @Get
    @Mapping("/getFile")
    public void getFile(Context ctx, @Param("id") Long id, @CurrentUserId Long userId) {
        try {
            var downloadedFile = fileOperationsFacade.getFile(id, userId);
            ctx.outputAsFile(downloadedFile);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 创建文件直链
     */
    @Post
    @Mapping("/createDirectLink")
    public DirectLinkCreateResultDTO createDirectLink(@Validated CreateDirectLinkVO createDirectLinkVO,
            @CurrentUserId Long userId) {
        return fileOperationsFacade.createDirectLink(createDirectLinkVO.getId(), createDirectLinkVO.getExpireDays(), userId);
    }

    /**
     * 撤销文件直链
     */
    @Post
    @Mapping("/revokeDirectLink")
    public Result<Void> revokeDirectLink(@Validated RevokeDirectLinkVO revokeDirectLinkVO, @CurrentUserId Long userId) {
        fileOperationsFacade.revokeDirectLink(revokeDirectLinkVO.getToken(), userId);
        return Result.success("撤销文件直链成功");
    }

    /**
     * 分页获取文件直链记录
     */
    @Get
    @Mapping("/getDirectLinkListByPage")
    public PageResult<DirectLinkRecordDTO> getDirectLinkListByPage(@Validated DirectLinkListVO directLinkListVO,
            @CurrentUserId Long userId) {
        return fileOperationsFacade.getDirectLinkListByPage(directLinkListVO.getPageNum(), directLinkListVO.getPageSize(), userId);
    }

    /**
     * 更新直链有效期
     */
    @Post
    @Mapping("/updateDirectLinkExpire")
    public DirectLinkCreateResultDTO updateDirectLinkExpire(@Validated UpdateDirectLinkExpireVO updateDirectLinkExpireVO,
            @CurrentUserId Long userId) {
        return fileOperationsFacade.updateDirectLinkExpire(updateDirectLinkExpireVO.getToken(), updateDirectLinkExpireVO.getExpireDays(), userId);
    }

    /**
     * 通过文件直链访问文件
     */
    @Get
    @Mapping("/direct")
    public void direct(Context ctx, @Param("token") String token, @Param("type") String type) {
        try {
            var downloadedFile = fileOperationsFacade.getFileByDirectLink(token, type);
            ctx.outputAsFile(downloadedFile);
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
     * @author bai
     */
    @Post
    @Mapping("/setFolderStorageSource")
    public Result<Void> setFolderStorageSource(@Validated SetFolderStorageSourceVO setFolderStorageSourceVO,
            @CurrentUserId Long userId) {
        fileOperationsFacade.setFolderStorageSource(setFolderStorageSourceVO, userId);
        return Result.success("设置文件夹存储源成功");
    }

    /**
     * 创建下载（自动判断直下或任务）
     *
     * @param ids 要下载的文件ID集合
     * @author bai
     */
    @Post
    @Mapping("/createDownload")
    public DownloadCreateResultDTO createDownload(@Validated @NotBlank(message = ERROR_DOWNLOAD_CONTENT_EMPTY) String ids,
            @CurrentUserId Long userId) {
        return fileOperationsFacade.createDownload(ids, userId);
    }

    /**
     * 获取下载任务状态
     *
     * @param taskId 任务ID
     * @author bai
     */
    @Get
    @Mapping("/getDownloadTask")
    public DownloadTaskStatusDTO getDownloadTask(
            @Validated @NotBlank(message = ERROR_TASK_ID_EMPTY) @Param("taskId") String taskId,
            @CurrentUserId Long userId) {
        return fileOperationsFacade.getDownloadTaskStatus(taskId, userId);
    }

    /**
     * 下载任务文件
     *
     * @param taskId 任务ID
     * @author bai
     */
    @Get
    @Mapping("/downloadTaskFile")
    public void downloadTaskFile(Context ctx,
            @Validated @NotBlank(message = ERROR_SIGN) @Param("sign") String sign) {
        DownloadTaskFileDTO taskFileDTO = fileOperationsFacade.downloadTaskFileBySign(sign);
        try {
            ctx.contentType("application/zip");
            ctx.headerSet("Content-Length", String.valueOf(taskFileDTO.getFile().length()));
            ctx.headerSet("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(taskFileDTO.getFileName(), StandardCharsets.UTF_8));
            try (InputStream in = new FileInputStream(taskFileDTO.getFile())) {
                ctx.output(in);
            }
            fileOperationsFacade.markDownloadTaskDownloaded(taskFileDTO.getTaskId());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new FailResultException(SYSTEM_ERROR);
        }
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
            var downloadedFile = fileOperationsFacade.downloadBySign(sign);
            ctx.outputAsFile(downloadedFile);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    // private void getImage2(HttpServletResponse response, String imageFolder,
    // String imageName) {
    // if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName) ||
    // StringTools.pathIsOk(imageFolder) || !StringTools.pathIsOk(imageName)) {
    // return;
    // }
    // String imageSuffix = StringTools.getFileSuffix(imageName);
    // String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE +
    // imageFolder + "/" + imageName;
    // imageSuffix = imageSuffix.replace(".", "");
    // String contentType = "image/" + imageSuffix;
    // response.setContentType(contentType);
    // response.setHeader("Cache-Control", "max-age=2592000");
    // readFile(response, filePath);
    // }

    /**
     * 获取视频
     */
    // @GetMapping("/ts/getVideoInfo/{fileId}")
    // public void getVideo(HttpServletResponse response, @PathVariable("fileId")
    // String fileId) {
    // getFile2(response, fileId, null);
    // }

    /**
     * 获取文件
     */
    // public void getFile2(HttpServletResponse response, String fileId, String
    // userId) {
    // String filePath = null;
    //
    // if (fileId.endsWith(".ts")) {
    // String[] tsArray = fileId.split("_");
    // String realFileId = tsArray[0];
    // FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(realFileId,
    // userId);
    // if (null == fileInfo) {
    // return;
    // }
    // String fileName = fileInfo.getFilePath();
    // fileName = StringTools.getFileNameNoSuffix(fileName) + "/" + fileId;
    // filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE +
    // fileName;
    // } else {
    // FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId,
    // userId);
    // if (null == fileInfo) {
    // return;
    // }
    // if (FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory()))
    // {
    // String fileNameNoSuffix =
    // StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
    // filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE +
    // fileNameNoSuffix + "/" + Constants.M3U8_NAME;
    // } else {
    // filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE +
    // fileInfo.getFilePath();
    // }
    //
    // File file = new File(filePath);
    // if (!file.exists()) {
    // return;
    // }
    // }
    //
    // readFile(response, filePath);
    // }

    /**
     * 获取文件
     */
    // @GetMapping("/getFile/{fileId}")
    // public void getFile(HttpServletResponse response, @PathVariable("fileId")
    // String fileId) {
    // getFile2(response, fileId, null);
    // }

    /**
     * 视频有根据文件ID获取面包屑导航的接口，视频中把所有文件信息都返回了，不合理，按自己的方式改造
     */
    // public void getFile() {
    // // TODO 待做，看视频
    // }

    /**
     * 预览资源缓存头 + ETag 条件请求
     */
    private boolean setCacheHeadersForPreview(Context ctx, File file, int maxAgeSeconds) {
        String etag = buildWeakEtag(file);
        String ifNoneMatch = ctx.header("If-None-Match");
        ctx.headerSet("Cache-Control", "public, max-age=" + maxAgeSeconds);
        ctx.headerSet("ETag", etag);
        if (etag.equals(ifNoneMatch)) {
            ctx.status(304);
            return true;
        }
        return false;
    }

    /**
     * 视频预览输出（支持单段 Range）
     */
    private void outputFileWithRangeSupport(Context ctx, File file, String fileName) throws IOException {
        long fileLength = file.length();
        ctx.headerSet("Accept-Ranges", "bytes");
        ctx.headerSet("Cache-Control", "public, max-age=600");
        ctx.headerSet("ETag", buildWeakEtag(file));
        String contentType = URLConnection.guessContentTypeFromName(fileName);
        if (contentType != null) {
            ctx.contentType(contentType);
        }

        String rangeHeader = ctx.header("Range");
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            ctx.headerSet("Content-Length", String.valueOf(fileLength));
            try (InputStream in = new FileInputStream(file)) {
                ctx.output(in);
            }
            return;
        }

        long[] range = parseRange(rangeHeader, fileLength);
        if (range == null) {
            ctx.status(416);
            ctx.headerSet("Content-Range", "bytes */" + fileLength);
            return;
        }

        long start = range[0];
        long end = range[1];
        long contentLength = end - start + 1;

        ctx.status(206);
        ctx.headerSet("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        ctx.headerSet("Content-Length", String.valueOf(contentLength));

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.skipNBytes(start);
            ctx.output(new LimitedInputStream(fis, contentLength));
        }
    }

    private long[] parseRange(String rangeHeader, long fileLength) {
        try {
            String rangeValue = rangeHeader.substring("bytes=".length()).trim();
            int dashIndex = rangeValue.indexOf("-");
            if (dashIndex < 0) {
                return null;
            }
            String startPart = rangeValue.substring(0, dashIndex).trim();
            String endPart = rangeValue.substring(dashIndex + 1).trim();

            long start;
            long end;
            if (startPart.isEmpty()) {
                long suffixLength = Long.parseLong(endPart);
                if (suffixLength <= 0) {
                    return null;
                }
                start = Math.max(0, fileLength - suffixLength);
                end = fileLength - 1;
            } else {
                start = Long.parseLong(startPart);
                end = endPart.isEmpty() ? fileLength - 1 : Long.parseLong(endPart);
            }

            if (start < 0 || end < start || start >= fileLength) {
                return null;
            }
            end = Math.min(end, fileLength - 1);
            return new long[] { start, end };
        } catch (Exception e) {
            return null;
        }
    }

    private String buildWeakEtag(File file) {
        return "W/\"" + file.lastModified() + "-" + file.length() + "\"";
    }

    private static final class LimitedInputStream extends FilterInputStream {
        private long remaining;

        private LimitedInputStream(InputStream in, long limit) {
            super(in);
            this.remaining = limit;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int b = super.read();
            if (b != -1) {
                remaining--;
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int max = (int) Math.min(len, remaining);
            int read = super.read(b, off, max);
            if (read > 0) {
                remaining -= read;
            }
            return read;
        }
    }
}