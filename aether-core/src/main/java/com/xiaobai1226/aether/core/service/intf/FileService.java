package com.xiaobai1226.aether.core.service.intf;

import com.xiaobai1226.aether.domain.entity.FileDO;
import com.xiaobai1226.aether.core.enums.FileTypeEnum;

/**
 * 文件服务接口
 *
 * @author bai
 */
public interface FileService {

    /**
     * 根据文件的MD5获取文件数据
     *
     * @param identifier 文件MD5唯一标识
     * @return 文件数据
     * @author bai
     */
    FileDO getFileByIdentifier(String identifier);

    /**
     * 新增一条File到数据库
     *
     * @param fileName   文件名
     * @param filePath   文件路径
     * @param fileSize   文件大小
     * @param identifier 文件MD5码
     * @param thumbnail  缩略图
     * @param fileType   文件类型
     * @return 主键ID
     */
    Integer addFile(String fileName, String filePath, Long fileSize, String identifier, String thumbnail, FileTypeEnum fileType);

    /**
     * 合并文件
     *
     * @param oldFileName 原文件名称
     * @param taskId      任务ID
     * @param tempFolder  临时文件目录
     * @return 最终文件路径
     * @author bai
     */
    String mergeFile(String oldFileName, String taskId, String tempFolder);

    /**
     * 根据ID获取文件数据
     *
     * @param id ID
     * @return
     */
    FileDO getFileById(Integer id);


//    Integer mergeFiless(Integer userId, String taskId);
}
