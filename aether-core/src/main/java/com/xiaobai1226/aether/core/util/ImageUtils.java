package com.xiaobai1226.aether.core.util;

import cn.hutool.core.io.FileUtil;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.xiaobai1226.aether.core.enums.OsEnum;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
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
            int sorceW = src.getWidth();
            int sorceH = src.getHeight();
            // 小于 指定宽度不压缩
            if (sorceW <= width) {
                FileUtil.copy(srcImagePath, destImagePath, true);
                return true;
            }

            if (!FileUtil.exist(destImagePath)) {
                FileUtil.touch(destImagePath);
            }

            String ffmpegPath = "bin/";
            switch (OsEnum.getCurrentOs()) {
                case WIN -> {
                    ffmpegPath = ffmpegPath + OsEnum.WIN.flag();
                    break;
                }
                case MAC -> {
                    ffmpegPath = ffmpegPath + OsEnum.MAC.flag();
                    break;
                }
            }

            URL res = ImageUtils.class.getClassLoader().getResource(ffmpegPath);
            if (res == null) {
                throw new Exception("路径不存在");
            }
            String path = Paths.get(res.toURI()).toAbsolutePath().toString();

            FFmpeg.atPath(Paths.get(path))
                    .addInput(UrlInput.fromPath(Paths.get(srcImagePath)))
                    .setOverwriteOutput(true)
                    .addArguments("-vf", "scale=" + width + ":-1") // 这里"-1"表示，高度将会自动按照输入视频的长宽比进行调整
                    .addOutput(UrlOutput.toPath(Paths.get(destImagePath)))
                    .execute();
        } catch (Exception e) {
            log.info("生成缩略图失败，失败原因：{}", e.getMessage());
            return false;
        }

        return true;
    }
}
