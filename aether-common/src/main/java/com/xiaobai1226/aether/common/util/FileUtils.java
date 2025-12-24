package com.xiaobai1226.aether.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.NioUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

/**
 * 文件工具类
 *
 * @author bai
 */
@Slf4j
public class FileUtils {

    /**
     * 生成路径
     *
     * @author bai
     */
    public static String generatePath(String rootPath, Object... keys) {
        StringBuilder keyBuilder = new StringBuilder(rootPath);
        for (Object key : keys) {
            keyBuilder.append(StrUtil.SLASH).append(key);
        }
        return keyBuilder.toString();
    }

    /**
     * 重命名文件或文件夹（重复文件）
     *
     * @param name 文件或文件夹名称
     * @return 新名称
     */
    public static String rename(String name) {
        var extName = FileNameUtil.extName(name);
        String randomString = DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);

        // 如果后缀不为空，则提取后缀名处理
        if (StrUtil.isNotEmpty(extName)) {
            var mainName = FileNameUtil.mainName(name);
            return mainName + StrUtil.UNDERLINE + randomString + StrUtil.DOT + extName;
        }

        return name + StrUtil.UNDERLINE + randomString;
    }

    /**
     * 重命名文件或文件夹（指定新名称）
     *
     * @param oldName 旧文件或文件夹名称
     * @param newName 新文件或文件夹名称
     * @return 新名称
     */
    public static String rename(String oldName, String newName) {
        var extName = FileNameUtil.extName(oldName);

        // 如果后缀不为空，则提取后缀名处理
        if (StrUtil.isNotEmpty(extName)) {
            return newName + StrUtil.DOT + extName;
        }

        return newName;
    }

    /**
     * 替换文件后缀名
     *
     * @param name    旧文件或文件夹名称
     * @param extName 拓展名
     * @return 新名称
     */
    public static String replaceFileExtName(String name, String extName) {
        var mainName = FileNameUtil.mainName(name);

//        return mainName + StrUtil.DOT + extName;
        return mainName + extName;
    }

    /**
     * 按名称顺序获取上传临时文件
     *
     * @param tempFolder 暂存临时目录
     * @return 文件数组
     */
    public static File[] getOrderedUploadTempFiles(String tempFolder) {
        // 获取文件夹中的文件列表
        var files = FileUtil.ls(tempFolder);

        // 对文件列表按照数字进行排序
        Arrays.sort(files, (file1, file2) -> {
            int index1 = Integer.parseInt(file1.getName());
            int index2 = Integer.parseInt(file2.getName());
            return index1 - index2;
        });

        return files;
    }

    /**
     * 合并文件
     *
     * @param sourceFiles 待合并的源文件数组
     * @param outputPath  合并后的文件路径
     */
    public static void mergeFiles(File[] sourceFiles, String outputPath) {
        File outputFile = new File(outputPath);
        if (!FileUtil.exist(outputFile.getParentFile())) {
            FileUtil.mkdir(outputFile.getParentFile());
        }

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputPath))) {
            for (File sourceFile : sourceFiles) {
                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile))) {
                    IoUtil.copy(inputStream, outputStream, NioUtil.DEFAULT_BUFFER_SIZE);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new IORuntimeException(e);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IORuntimeException(e);
        }
    }
}
