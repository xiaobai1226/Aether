package com.xiaobai1226.aether.core.service.support;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.constant.SystemConsts;
import com.xiaobai1226.aether.common.enums.CategoryEnum;
import com.xiaobai1226.aether.common.enums.FileTypeEnum;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.common.util.ImageUtils;
import com.xiaobai1226.aether.common.util.VideoUtils;
import com.xiaobai1226.aether.core.domain.dto.ThumbnailGenerationResultDTO;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.DownloadedFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 缩略图服务类
 * 
 * 负责处理文件缩略图的生成、查询、删除等操作
 * 
 * @author bai
 */
@Slf4j
@Component
public class ThumbnailService {

    /**
     * 缩略图宽度（像素）
     */
    private static final int THUMBNAIL_WIDTH = 150;

    /**
     * 大视频文件不生成缩略图的阈值（100MB）
     */
    private static final long MAX_VIDEO_SIZE_FOR_THUMBNAIL = 100 * 1024 * 1024;

    /**
     * 项目根路径（从配置文件注入）
     */
    @Inject("${project.path.root}")
    private String rootPath;

    /**
     * 为文件生成缩略图（带业务判断）
     * 
     * 根据文件类型（图片/视频）自动选择合适的缩略图生成策略
     * 会自动判断是否需要生成缩略图（文件类型、大小等）
     * 
     * @param sourceFilePath 源文件的绝对路径
     * @param fileName       文件名（用于判断文件类型）
     * @param fileSize       文件大小（字节）
     * @return 缩略图生成结果，包含缩略图文件名和完整路径；如果不需要生成则返回失败结果
     */
    public ThumbnailGenerationResultDTO generateThumbnail(String sourceFilePath, String fileName, Long fileSize) {
        // 判断是否需要生成缩略图
        if (!needsThumbnail(fileName, fileSize)) {
            log.debug("文件不需要生成缩略图: fileName={}, fileSize={}", fileName, fileSize);
            return new ThumbnailGenerationResultDTO(false, null, null);
        }

        return generateThumbnailInternal(sourceFilePath, fileName);
    }

    /**
     * 为文件生成缩略图（不带业务判断，直接生成）
     * 
     * 根据文件类型（图片/视频）自动选择合适的缩略图生成策略
     * 
     * @param sourceFilePath 源文件的绝对路径
     * @param fileName       文件名（用于判断文件类型）
     * @return 缩略图生成结果，包含缩略图文件名和完整路径
     */
    public ThumbnailGenerationResultDTO generateThumbnailInternal(String sourceFilePath, String fileName) {
        if (StrUtil.isBlank(sourceFilePath) || StrUtil.isBlank(fileName)) {
            log.warn("缩略图生成参数不完整: sourceFilePath={}, fileName={}",
                    sourceFilePath, fileName);
            return new ThumbnailGenerationResultDTO(false, null, null);
        }

        // 判断源文件是否存在
        if (!FileUtil.exist(sourceFilePath)) {
            log.warn("源文件不存在，无法生成缩略图: {}", sourceFilePath);
            return new ThumbnailGenerationResultDTO(false, null, null);
        }

        // 根据文件类型确定缩略图后缀
        var thumbnailSuffix = FileTypeEnum.isGif(FileNameUtil.extName(fileName).toLowerCase())
                ? SystemConsts.THUMBNAIL_GIF_SUFFIX
                : SystemConsts.THUMBNAIL_SUFFIX;

        // 生成缩略图相对文件名（按日期分目录存储）
        String thumbnailFileName = DateUtil.format(new Date(), "yyyy/MM/dd") + StrUtil.SLASH
                + FileUtils.replaceFileExtName(fileName, thumbnailSuffix);

        // 生成缩略图完整路径
        var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL,
                thumbnailFileName);

        // 确保缩略图目录存在
        FileUtil.mkParentDirs(thumbnailFilePath);

        boolean result = false;

        // 根据文件类型生成缩略图
        if (CategoryEnum.isPictureByName(fileName)) {
            // 图片生成缩略图（宽度150px，高度自适应）
            result = ImageUtils.generateThumbnail(sourceFilePath, thumbnailFilePath, THUMBNAIL_WIDTH, -1);
            if (result) {
                log.debug("图片缩略图生成成功: {} -> {}", sourceFilePath, thumbnailFilePath);
            } else {
                log.warn("图片缩略图生成失败: {}", sourceFilePath);
            }
        } else if (CategoryEnum.isVideoByName(fileName)) {
            // 视频生成缩略图（提取第一帧）
            result = VideoUtils.generateThumbnail(sourceFilePath, thumbnailFilePath, THUMBNAIL_WIDTH);
            if (result) {
                log.debug("视频缩略图生成成功: {} -> {}", sourceFilePath, thumbnailFilePath);
            } else {
                log.warn("视频缩略图生成失败: {}", sourceFilePath);
            }
        } else {
            // 其他类型文件不生成缩略图
            log.debug("文件类型不支持缩略图: {}", fileName);
            return new ThumbnailGenerationResultDTO(false, null, null);
        }

        // 如果生成失败，返回失败结果
        if (!result) {
            return new ThumbnailGenerationResultDTO(false, null, null);
        }

        return new ThumbnailGenerationResultDTO(true, thumbnailFileName, thumbnailFilePath);
    }

    /**
     * 删除缩略图文件
     * 
     * @param thumbnailFileName 缩略图文件名（相对路径）
     */
    public void deleteThumbnail(String thumbnailFileName) {
        if (StrUtil.isBlank(thumbnailFileName)) {
            log.warn("删除缩略图参数不完整: thumbnailFileName={}", thumbnailFileName);
            return;
        }

        var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL,
                thumbnailFileName);

        if (FileUtil.exist(thumbnailFilePath)) {
            FileUtil.del(thumbnailFilePath);
            log.debug("缩略图已删除: {}", thumbnailFilePath);
        } else {
            log.debug("缩略图文件不存在，无需删除: {}", thumbnailFilePath);
        }
    }

    /**
     * 获取缩略图文件
     * 
     * @param thumbnailFileName 缩略图文件名（相对路径）
     * @return 缩略图文件
     * @throws IOException 如果文件读取失败
     */
    public DownloadedFile getThumbnailFile(String thumbnailFileName) throws IOException {
        if (StrUtil.isBlank(thumbnailFileName)) {
            log.warn("获取缩略图参数为空");
            return null;
        }

        var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL,
                thumbnailFileName);
        
        if (!FileUtil.exist(thumbnailFilePath)) {
            log.warn("缩略图文件不存在: {}", thumbnailFilePath);
            return null;
        }

        File file = FileUtil.file(thumbnailFilePath);
        DownloadedFile downloadedFile = new DownloadedFile(file);
        downloadedFile.asAttachment(false);
        return downloadedFile;
    }

    /**
     * 判断文件是否需要生成缩略图
     * 
     * @param fileName 文件名
     * @param fileSize 文件大小（字节）
     * @return 是否需要生成缩略图
     */
    public static boolean needsThumbnail(String fileName, Long fileSize) {
        if (StrUtil.isBlank(fileName)) {
            return false;
        }

        String suffix = FileNameUtil.extName(fileName);
        if (StrUtil.isBlank(suffix)) {
            return false;
        }

        // 图片和视频需要缩略图
        if (!CategoryEnum.isPictureBySuffix(suffix) && !CategoryEnum.isVideoBySuffix(suffix)) {
            return false;
        }

        // HEIC格式特殊处理（已在获取图片时转换）
        if (FileTypeEnum.isHeic(suffix)) {
            return false;
        }

        // 文件不能太大（超过100MB的视频不生成缩略图）
        if (CategoryEnum.isVideoBySuffix(suffix) && fileSize != null && fileSize > MAX_VIDEO_SIZE_FOR_THUMBNAIL) {
            return false;
        }

        return true;
    }
}