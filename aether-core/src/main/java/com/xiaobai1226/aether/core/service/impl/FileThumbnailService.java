package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.enums.CategoryEnum;
import com.xiaobai1226.aether.common.enums.FileTypeEnum;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.common.util.ImageUtils;
import com.xiaobai1226.aether.common.util.VideoUtils;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.io.File;

/**
 * 文件缩略图服务
 * 
 * 职责：生成和管理文件缩略图
 * 
 * @author bai
 */
@Component
@Slf4j
public class FileThumbnailService {

    @Inject("${project.path.root}")
    private String rootPath;

    /**
     * 生成缩略图（如果需要）
     * 
     * @param file     源文件
     * @param suffix   文件后缀
     * @param fileSize 文件大小
     * @return 缩略图相对路径，不需要生成则返回null
     */
    public String generateThumbnail(File file, String suffix, Long fileSize) {
        if (file == null || suffix == null) {
            return null;
        }

        try {
            // 判断是否需要生成缩略图
            if (!needsThumbnail(suffix, fileSize)) {
                return null;
            }

            // 生成缩略图文件名
            String thumbnailFileName = IdUtil.simpleUUID() + ".jpg";
            String thumbnailPath = FileUtils.generatePath(
                    rootPath,
                    FolderNameConsts.PATH_THUMBNAIL_FILE_FULL,
                    thumbnailFileName
            );

            // 根据文件类型生成缩略图
            boolean success = false;
            if (CategoryEnum.isPictureBySuffix(suffix)) {
                success = generateImageThumbnail(file.getAbsolutePath(), thumbnailPath);
            } else if (CategoryEnum.isVideoBySuffix(suffix)) {
                success = generateVideoThumbnail(file.getAbsolutePath(), thumbnailPath);
            }

            if (success) {
                log.info("缩略图生成成功: {}", thumbnailFileName);
                return thumbnailFileName;
            } else {
                log.warn("缩略图生成失败: suffix={}", suffix);
                return null;
            }

        } catch (Exception e) {
            log.error("生成缩略图异常: suffix={}", suffix, e);
            return null;
        }
    }

    /**
     * 删除缩略图
     * 
     * @param thumbnail 缩略图相对路径
     */
    public void deleteThumbnail(String thumbnail) {
        if (thumbnail == null) {
            return;
        }

        try {
            String thumbnailPath = FileUtils.generatePath(
                    rootPath,
                    FolderNameConsts.PATH_THUMBNAIL_FILE_FULL,
                    thumbnail
            );

            if (FileUtil.exist(thumbnailPath)) {
                FileUtil.del(thumbnailPath);
                log.info("缩略图已删除: {}", thumbnail);
            }
        } catch (Exception e) {
            log.warn("删除缩略图失败: {}", thumbnail, e);
        }
    }

    /**
     * 判断是否需要生成缩略图
     */
    private boolean needsThumbnail(String suffix, Long fileSize) {
        // 图片和视频需要缩略图
        if (!CategoryEnum.isPictureBySuffix(suffix) && !CategoryEnum.isVideoBySuffix(suffix)) {
            return false;
        }

        // HEIC格式特殊处理（已在获取图片时转换）
        if (FileTypeEnum.isHeic(suffix)) {
            return false;
        }

        // 文件不能太大（超过100MB的视频不生成缩略图）
        if (CategoryEnum.isVideoBySuffix(suffix) && fileSize > 100 * 1024 * 1024) {
            return false;
        }

        return true;
    }

    /**
     * 生成图片缩略图
     */
    private boolean generateImageThumbnail(String sourcePath, String thumbnailPath) {
        try {
            return ImageUtils.generateThumbnail(sourcePath, thumbnailPath, 200, -1);
        } catch (Exception e) {
            log.error("生成图片缩略图失败", e);
            return false;
        }
    }

    /**
     * 生成视频缩略图
     */
    private boolean generateVideoThumbnail(String sourcePath, String thumbnailPath) {
        try {
            return VideoUtils.generateThumbnail(sourcePath, thumbnailPath, 200);
        } catch (Exception e) {
            log.error("生成视频缩略图失败", e);
            return false;
        }
    }
}