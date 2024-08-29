package com.xiaobai1226.aether.core.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.NioUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.core.constant.FolderNameConsts;
import com.xiaobai1226.aether.core.domain.entity.FileDO;
import com.xiaobai1226.aether.core.enums.FileTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.handle.Context;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

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
        String time = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        // 如果后缀不为空，则提取后缀名处理
        if (StrUtil.isNotEmpty(extName)) {
            var mainName = FileNameUtil.mainName(name);
            return mainName + StrUtil.UNDERLINE + time + StrUtil.DOT + extName;
        }

        return name + StrUtil.UNDERLINE + time;
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
                    throw new IORuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * 将文件写入到输出流中
     */
    public static void readFile(Context context, String filePath) {
        if (!FileUtil.exist(filePath)) {
            return;
        }
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = context.outputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("关闭输出流异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("关闭输入流异常", e);
                }
            }
        }
    }

    public static void readFileByResources(Context context, String resourcePath) {

        OutputStream out = null;
        InputStream in = null;
        try {
            in = ImageUtils.class.getResourceAsStream(resourcePath);
            if (in == null) {
                return;
            }
            byte[] byteData = new byte[1024];
            out = context.outputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("关闭输出流异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("关闭输入流异常", e);
                }
            }
        }
    }

// TODO 暂时注释掉
//    public static void printNoDefaultImage(HttpServletResponse response) {
//        response.setHeader("Content-type", "text/html;charset=UTF-8");
//        response.setStatus(HttpStatus.OK.value());
//        PrintWriter writer = null;
//        try {
//            writer = response.getWriter();
//            writer.write("请在头像目录下放至默认头像文件");
//        } catch (IOException e) {
//            log.error("输出没有默认头像异常", e);
//        } finally {
//            if (writer != null) {
//                writer.close();
//            }
//        }
//    }

    public static void deleteFile(FileDO fileDO, String rootPath) {
        // 删除缩略图
        if (Objects.equals(FileTypeEnum.PICTURE.id(), fileDO.getFileType()) || Objects.equals(FileTypeEnum.VIDEO.id(), fileDO.getFileType())) {
            // 设置文件存储全路径
            var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL, fileDO.getThumbnail());
            if (FileUtil.exist(thumbnailFilePath)) {
                FileUtil.del(thumbnailFilePath);
            }
        }

        if (FileUtil.exist(fileDO.getPath())) {
            FileUtil.del(fileDO.getPath());
        }
    }
}
