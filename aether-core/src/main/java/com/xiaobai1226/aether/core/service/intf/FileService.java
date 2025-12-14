package com.xiaobai1226.aether.core.service.intf;

import com.xiaobai1226.aether.dao.domain.entity.FileDO;

import java.util.List;

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
     * 根据文件的MD5获取文件数据列表（可能存在多个不同存储源的相同文件）
     *
     * @param identifier 文件MD5唯一标识
     * @return 文件数据列表
     * @author bai
     */
    List<FileDO> getFileListByIdentifier(String identifier);

    /**
     * 新增一条File到数据库
     *
     * @param fileName        文件名
     * @param filePath        文件路径
     * @param fileSize        文件大小
     * @param identifier      文件MD5码
     * @param thumbnail       缩略图
     * @param storageSourceId 存储源ID
     * @return 主键ID
     */
    FileDO addFile(String fileName, String filePath, Long fileSize, String identifier, String thumbnail, Long storageSourceId);

    /**
     * 合并文件
     *
     * @param oldFileName     原文件名称
     * @param taskId          任务ID
     * @param tempFolder      临时文件目录
     * @param storagePath     存储源路径
     * @return 最终文件路径
     * @author bai
     */
    String mergeFile(String oldFileName, String taskId, String tempFolder, String storagePath);

    /**
     * 根据ID获取文件数据
     *
     * @param id ID
     */
    FileDO getFileById(Long id);

    /**
     * 复制文件到目标存储源
     *
     * @param sourceFileDO      源文件DO对象
     * @param sourceStoragePath 源存储源路径
     * @param targetStoragePath 目标存储源路径
     * @param targetStorageId   目标存储源ID
     * @return 新创建的文件DO对象
     * @author bai
     */
    FileDO copyFileToStorageSource(FileDO sourceFileDO, String sourceStoragePath, String targetStoragePath,
            Long targetStorageId);

//    Integer mergeFiless(Integer userId, String taskId);
}