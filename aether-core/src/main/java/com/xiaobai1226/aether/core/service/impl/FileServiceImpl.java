package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.solon.conditions.query.LambdaQueryChainWrapper;
import com.xiaobai1226.aether.core.constant.FolderNameConsts;
import com.xiaobai1226.aether.core.dao.redis.FileRedisDAO;
import com.xiaobai1226.aether.core.domain.entity.FileDO;
import com.xiaobai1226.aether.core.enums.FileTypeEnum;
import com.xiaobai1226.aether.core.mapper.FileMapper;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.util.FileUtils;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.io.File;
import java.util.Date;

/**
 * 文件服务接口实现类
 *
 * @author bai
 */
@Component
public class FileServiceImpl implements FileService {

    @Db
    private FileMapper fileMapper;

    @Inject
    private FileRedisDAO fileRedisDAO;

    @Inject("${project.path.root}")
    private String rootPath;

    @Override
    public FileDO getFileByIdentifier(String identifier) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(fileMapper);
        return lambdaQuery.eq(FileDO::getIdentifier, identifier).one();
    }

    @Override
    public Integer addFile(String fileName, String filePath, Long fileSize, String identifier, String thumbnail, FileTypeEnum fileType) {
        var fileDO = new FileDO();
        fileDO.setName(fileName);
        fileDO.setPath(filePath);
        fileDO.setSize(fileSize);

        if (thumbnail != null) {
            fileDO.setThumbnail(thumbnail);
        }

        fileDO.setFileType(fileType.id());
        // TODO 修改为转码中状态
//        fileDO.setFileStatus(1);
        fileDO.setIdentifier(identifier);
//        fileDO.setStorageSourceId(LOCAL.getId());

        var resultCount = fileMapper.insert(fileDO);

        if (resultCount == 1) {
            return fileDO.getId();
        }

        return null;
    }

    @Override
    public String mergeFile(String oldFileName, String taskId, String tempFolder) {
        // 设置文件名
        var newFileName = FileUtils.rename(oldFileName, taskId);
        // 设置文件存储全路径
        var filePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_UPLOAD_FILE_FULL, DateUtil.format(new Date(), "yyyy/MM/dd"), newFileName);

        // 获取分片文件夹中的所有文件
        File[] files = FileUtils.getOrderedUploadTempFiles(tempFolder);

        // 合并分片文件
        FileUtils.mergeFiles(files, filePath);

        return filePath;
    }

    @Override
    public FileDO getFileById(Integer id) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(fileMapper);
        lambdaQuery.eq(FileDO::getId, id);

        return lambdaQuery.one();
    }

//    @Override
//    public Integer mergeFiless(Integer userId, String taskId) {
//        // 获取缓存中数据
//        var uploadTempFileDTO = fileRedisDAO.getUploadTempFileInfo(userId, taskId);
//
//        if (uploadTempFileDTO == null) {
//            // TODO 返回错误信息
//            return null;
//        }
////        else if () {
//// TODO 判断临时文件目录是否存在，且是否有文件
////        } else if(){
//
////        }
//        // 设置文件名
//        var fileName = FileUtils.rename(uploadTempFileDTO.getFileName(), taskId);
//        // 设置文件存储全路径
//        var filePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_UPLOAD_FILE_FULL, DateUtil.format(new Date(), "yyyy/MM/dd"), fileName);
//
//        // 获取分片文件夹中的所有文件
//        File[] files = FileUtils.getOrderedUploadTempFiles(uploadTempFileDTO.getTempFolder());
//
//        // 合并分片文件
//        FileUtils.mergeFiles(files, filePath);
//
//        Long finalFileSize = FileUtil.size(new File(filePath));

//        if (FileTypeEnums.VIDEO == fileTypeEnum) {
//            cutFile4Video(fileId, targetFilePath);
//            // 视频生成缩略图
//            cover = month + "/" + currentUserFolderName + Contents.IMAGE_PNG_SUFFIX;
//            String coverPath = targetFolderName + "/" + cover;
//            ScaleFilter.createCover4Video(new File(targetFilePath), 150, new File(coverPath));
//        } else if (FileTypeEnums.IMAGE == fileTypeEnum) {
//            // 生成缩略图
//            cover = month + "/" + realFileName.replace(".", "_.");
//            String coverPath = targetFolderName + "/" + cover;
//            Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath), 150, new File(coverPath), false);
//            if (!created) {
//                FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
//            }
//        }

    /** TODO 如果是视频文件则生成视频缩略图及切片（重新看视频，需要装ffmpeg）
     openai的生成缩略图答案
     要使用Java生成视频缩略图，可以使用Xuggler这个开源库（基于FFmpeg）。这里有一个简单的示例，说明如何生成一个视频文件的缩略图：

     1. 首先，确保将Xuggler库添加到您的项目中。如果您使用的是Maven，可以在pom.xml中添加以下依赖项：

     <dependency>
     <groupId>com.xuggle</groupId>
     <artifactId>xuggle-xuggler</artifactId>
     <version>5.4</version>
     </dependency>

     1. 下面是一个简单的Java代码示例，该示例说明如何使用Xuggler生成视频缩略图（单个帧的图像）：

     import java.awt.image.BufferedImage;
     import java.io.File;
     import java.io.IOException;
     import javax.imageio.ImageIO;
     import com.xuggle.mediatool.IMediaReader;
     import com.xuggle.mediatool.MediaListenerAdapter;
     import com.xuggle.mediatool.ToolFactory;
     import com.xuggle.mediatool.event.IVideoPictureEvent;

     public class VideoThumbnail {

     public static void main(String[] args) {
     String inputVideoPath = "path/to/your/video/file.mp4";
     String outputImagePath = "path/to/your/thumbnail/output.jpg";

     createVideoThumbnail(inputVideoPath, outputImagePath);
     }

     public static void createVideoThumbnail(String inputVideoPath, String outputImagePath) {
     IMediaReader mediaReader = ToolFactory.makeReader(inputVideoPath);
     mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

     mediaReader.addListener(new MediaListenerAdapter() {
    @Override public void onVideoPicture(IVideoPictureEvent event) {
    if (event.getStreamIndex() != 0) {
    return;
    }
    File outputImageFile = new File(outputImagePath);
    if (!outputImageFile.exists()) {
    try {
    ImageIO.write(event.getImage(), "jpg", outputImageFile);
    } catch (IOException e) {
    e.printStackTrace();
    }
    mediaReader.close();
    }
    }
    });

     while (mediaReader.readPacket() == null) {
     // do nothing
     }
     }
     }

     在这个示例中，请将 inputVideoPath 设置为要提取缩略图的视频文件路径，将 outputImagePath 设置为要保存生成的缩略图的文件路径。运行此代码，它将从视频的第一帧生成一个缩略图并将其保存为JPG格式的图像。

     您可以根据需求对此代码进行相应的修改，例如获取特定时间戳的缩略图等。


     openai切片方案
     要使用Java对视频进行切片，可以使用FFmpeg Java库（Jaffree）。Jaffree允许在Java应用程序中方便调用FFmpeg命令并对视频执行各种操作，例如切片、转码等。

     1. 首先，将Jaffree库添加到您的项目中。如果您使用的是Maven，可以在pom.xml中添加以下依赖项：

     <dependency>
     <groupId>com.github.hiteshsondhi88.libffmpeg</groupId>
     <artifactId>FFmpegAndroid</artifactId>
     <version>0.3.2</version>
     </dependency>

     1. 下载并安装FFmpeg。请确保您已将其添加到系统的环境变量中。如果需要获取FFmpeg，可以在官网下载： https://www.ffmpeg.org/download.html
     2. 您可以使用下面的代码将视频切片，以便后续播放：

     import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
     import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
     import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

     public class VideoSlicer {

     public static void main(String[] args) {
     String inputVideoPath = "path/to/your/video/file.mp4";
     String outputVideoPath = "path/to/your/output/sliced_video_file.mp4";
     int startTime = 5; // 起始时间，秒
     int duration = 10; // 切片的持续时间，秒

     sliceVideo(inputVideoPath, outputVideoPath, startTime, duration);
     }

     public static void sliceVideo(String inputVideoPath, String outputVideoPath, int startTime, int duration) {
     // 基于输入视频路径获取FFmpeg实例
     FFmpeg ffmpeg = FFmpeg.getInstance(getActivity());

     String[] command = {
     "-i", inputVideoPath, // 输入文件
     "-ss", String.valueOf(startTime), // 起始时间
     "-t", String.valueOf(duration), // 切片持续时间
     "-c", "copy", // 复制编解码器设置，避免重新编码
     "-y", // 覆盖现有文件
     outputVideoPath // 输出文件
     };

     ffmpeg..execute(command, new FFmpegExecuteResponseHandler() {
    @Override public void onSuccess(String s) {
    System.out.println("Video sliced successfully");
    }

    @Override public void onProgress(String s) {
    System.out.println("Slicing progress: " + s);
    }

    @Override public void onFailure(String s) {
    System.out.println("Slicing failed");
    }

    @Override public void onStart() {
    System.out.println("Slicing started");
    }

    @Override public void onFinish() {
    System.out.println("Slicing finished");
    }
    });
     }
     }

     在此示例中，将inputVideoPath设置为要切片的视频文件路径，将outputVideoPath设置为要保存切片视频的文件路径。此代码会创建一个新的切片视频文件，其中包含从startTime开始的，时长为duration秒的视频片段。可以根据需求调整这些值。 运行此代码，它将根据给定的时间和时长参数执行视频切片，并保存输出文件。

     请注意，这里的示例代码使用了GitHub上公开的FFmpegAndroid库（https://github.com/hiteshsondhi88/FFmpegAndroid）为例。还有其他可用的Java库可以执行类似的操作，如Jaffree（https://github.com/kokorin/Jaffree）。可以根据项目需求选择合适的库。
     */

    // TODO 如果是图片文件则生成视频缩略图 用hutool工具类

    // 写入File库
//        return addFile(fileName, filePath, finalFileSize, uploadTempFileDTO.getIdentifier());
//    }

//    private void cutFile4Video(String fileId, String videoFilePath) {
//        // 创建同名切片目录
//        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
//        if (!tsFolder.exists()) {
//            tsFolder.mkdirs();
//        }
//        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
//        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
//        String tsPath = tsFolder + "/" + Contents.TS_NAME;
//        // 生成.ts
//        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
//        ProcessUtils.executeCommand(cmd, false);
//        // 生成索引文件.m3u8和切片.ts
//        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Contents.M3U8_NAME, tsFolder.getPath(), fileId);
//        ProcessUtils.executeCommand(cmd, false);
//        // 删除index.ts
//        new File(tsPath).delete();
//    }
}
