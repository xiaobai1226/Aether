package com.xiaobai1226.aether.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.xiaobai1226.aether.common.enums.FileTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;

/**
 * 图片工具类
 */
@Slf4j
public class ImageUtils {

    /**
     * 生成缩略图
     * TODO svg、webp格式文件缩略图待完善
     */
    public static Boolean generateThumbnail(String srcImagePath, String destImagePath, int width, int height) {
        // 获取宽度命令
//        String getWidthCommand = String.format("identify -format \"%%w\" %s", srcImagePath);
        // ImageMagick生成缩略图命令
        String imageMagickCommand = String.format("convert %s -resize 150x -quality 80 %s", srcImagePath, destImagePath);
//        String ffmpegCommand = String.format("ffmpeg -i %s -vf scale=150:-1 -qscale:v 5 %s", srcImagePath, destImagePath);

        try {
            // 执行命令并获取图片宽度
//            String widthStr = RuntimeUtil.execForStr(getWidthCommand);
//            Integer imgWidth = null;
//
//            try {
//                // 转换为整数
//                imgWidth = NumberUtil.parseInt(StrUtil.removeAll(widthStr, '"').trim());
//            } catch (NumberFormatException e) {
//                log.error("获取图片宽度失败，无法解析宽度值，宽度返回结果为：" + widthStr);
//            }
//
//            // 小于指定宽度不压缩
//            if (imgWidth != null && imgWidth <= width) {
//                FileUtil.copy(srcImagePath, destImagePath, true);
//                return true;
//            }

            if (!FileUtil.exist(destImagePath)) {
                FileUtil.touch(destImagePath);
            }

            var suffix = FileNameUtil.extName(srcImagePath);
            // 如果是svg
            if (StrUtil.isNotEmpty(suffix) && FileTypeEnum.isSvg(suffix.toLowerCase())) {
                FFmpeg.atPath()
                        .addInput(UrlInput.fromPath(Paths.get(srcImagePath)))
                        .setOverwriteOutput(true)
                        .addArguments("-vf", "scale=" + width + ":-1")
                        .addArguments("-qscale:v", "5")
                        .addOutput(UrlOutput.toPath(Paths.get(destImagePath)))
                        .execute();

                return true;
            }

            // 执行命令
            Process process = RuntimeUtil.exec(imageMagickCommand);

            // 等待命令执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String errorOutput = RuntimeUtil.getErrorResult(process);
                log.error("生成缩略图失败，失败原因：" + errorOutput);
                FileUtil.del(destImagePath);
                return false;
            }
        } catch (Exception e) {
            log.error("生成缩略图失败，失败原因：{}", e.getMessage());
            FileUtil.del(destImagePath);
            return false;
        }

        return true;
    }

    /**
     * Heic转为Webp格式
     */
    public static byte[] heic2Webp(String srcImagePath) {
        // 构建 ImageMagick 命令，输出到标准输出流
        String command = String.format("convert %s -quality 85 WEBP:-", srcImagePath);

        try {
            // 执行命令
            Process process = RuntimeUtil.exec(command);

            // 使用 Hutool 的流工具类简化读取（替代手动循环）
            var inputStream = process.getInputStream();
            byte[] webpBytes = IoUtil.readBytes(inputStream);

            // 等待命令执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String errorOutput = RuntimeUtil.getErrorResult(process);
                log.error("Heic转Webp失败，错误信息：" + errorOutput);
                return null;
            }

            // 返回 WebP 图片的字节数组
            return webpBytes;
        } catch (Exception e) {
            log.error("Heic转Webp失败，发生异常：" + e.getMessage());
            return null;
        }
    }
}
