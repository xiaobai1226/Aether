//package com.xiaobai1226.aether.core.util;
//
//import java.io.File;
//
//public class ScaleFilter {
//    public static void createCover4Video(File sourceFile, Integer width, File targetFile) {
//        try {
//            String cmd = "ffmpeg -i %s -y -vframes 1 -vf scale=%d:%d/a %s";
//            ProcessUtils.executeCommand(String.format(cmd, sourceFile.getAbsolutePath(), width, width, targetFile.getAbsolutePath()), false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static Boolean createThumbnailWidthFFmpeg(File file, Integer width, File targetFile, Boolean delSource) {
//        try {
//            BufferdImage src = ImageIO.read(file);
//            int sorceW = src.getWidth();
//            int sorceH = src.getHeight();
//            // 小于 指定高度不压缩
//            if (sorceW <= width) {
//                return false;
//            }
//            compressImage(file, width, targetFile, delSource);
//            return true;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static void compressImage(File sourceFile, Integer width, File targetFile, Boolean delSource) {
//        try {
//            String cmd = "ffmpeg -i %s -vf scale=%d:-1 %s -y";
//            ProcessUtils.executeCommand(String.format(cmd, sourceFile.getAbsolutePath(), width, targetFile.getAbsolutePath()), false);
//            if (delSource) {
//                FileUtils.forceDelete(sourceFile);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
