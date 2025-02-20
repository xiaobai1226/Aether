package com.xiaobai1226.aether.common.util;

import cn.hutool.core.io.FileUtil;
import com.github.kokorin.jaffree.ffmpeg.*;
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

            // 创建FilterGraph，包含thumbnail滤镜和scale滤镜
            FilterChain filterChain = FilterChain.of(
                    Filter.withName("thumbnail") // 使用thumbnail滤镜选择关键帧
                            .addArgument("n", "100"), // 分析前100帧
                    Filter.withName("scale") // 使用scale滤镜调整尺寸
                            .addArgument("w", String.valueOf(width)) // 设置宽度
                            .addArgument("h", "-1") // 高度自适应
            );

            // 创建FilterGraph
            FilterGraph filterGraph = FilterGraph.of(filterChain);

            FFmpeg.atPath()
                    .addInput(UrlInput.fromPath(Paths.get(srcFilePath)))
                    .setOverwriteOutput(true)
                    .setComplexFilter(filterGraph)
                    .addArguments("-vframes", "1") // 选取一帧
                    .addOutput(UrlOutput.toPath(Paths.get(destFilePath)))
                    .execute();
        } catch (Exception e) {
            log.error("生成缩略图失败，失败原因：{}", e.getMessage());
            FileUtil.del(destFilePath);
            return false;
        }
        return true;
    }
}
