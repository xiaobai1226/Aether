package com.xiaobai1226.aether.common.util;

import cn.hutool.core.io.FileUtil;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;

/**
 * 视频工具类
 */
@Slf4j
public class VideoUtils {

    /**
     * 生成缩略图
     */
    public static Boolean generateThumbnail(String srcFilePath, String destFilePath, int width) {
        try {
            if (!FileUtil.exist(destFilePath)) {
                FileUtil.touch(destFilePath);
            }

//           String ffmpegPath = "bin/";
//           switch (OsEnum.getCurrentOs()) {
//               case OsEnum.WIN -> {
//                   ffmpegPath = ffmpegPath + OsEnum.WIN.flag();
//                   break;
//               }
//               case OsEnum.MAC -> {
//                   ffmpegPath = ffmpegPath + OsEnum.MAC.flag();
//                   break;
//               }
//           }
//
//           URL res = VideoUtils.class.getClassLoader().getResource(ffmpegPath);
//           if (res == null) {
//               throw new Exception("路径不存在");
//           }
//           String path = Paths.get(res.toURI()).toAbsolutePath().toString();

            FFmpeg.atPath().addInput(UrlInput.fromPath(Paths.get(srcFilePath))).setOverwriteOutput(true).addArguments("-vframes", "1") // 选取一帧
                    .addArguments("-vf", "scale=" + width + ":-1") // 这里"-1"表示，高度将会自动按照输入视频的长宽比进行调整
                    .addOutput(UrlOutput.toPath(Paths.get(destFilePath))).execute();
        } catch (Exception e) {
            log.info("生成缩略图失败，失败原因：{}", e.getMessage());
            FileUtil.del(destFilePath);
            return false;
        }
        return true;
    }
}
