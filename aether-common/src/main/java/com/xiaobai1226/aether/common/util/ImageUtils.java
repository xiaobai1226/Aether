package com.xiaobai1226.aether.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
        try {
            BufferedImage src = ImageIO.read(FileUtil.file(srcImagePath));
            if (src != null) {
                int sorceW = src.getWidth();
                int sorceH = src.getHeight();
                // 小于 指定宽度不压缩
                if (sorceW <= width) {
                    FileUtil.copy(srcImagePath, destImagePath, true);
                    return true;
                }
            }

            if (!FileUtil.exist(destImagePath)) {
                FileUtil.touch(destImagePath);
            }

            FFmpeg.atPath().addInput(UrlInput.fromPath(Paths.get(srcImagePath))).setOverwriteOutput(true).addArguments("-vf", "scale=" + width + ":-1").addArguments("-q:v", "2") // 这里"-1"表示，高度将会自动按照输入视频的长宽比进行调整
                    .addOutput(UrlOutput.toPath(Paths.get(destImagePath))).execute();
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
