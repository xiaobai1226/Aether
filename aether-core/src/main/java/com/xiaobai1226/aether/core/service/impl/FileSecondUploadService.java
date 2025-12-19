package com.xiaobai1226.aether.core.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.Objects;

/**
 * 文件秒传服务
 * 
 * 职责：处理文件秒传逻辑，判断文件是否已存在并可复用
 * 
 * @author bai
 */
@Component
@Slf4j
public class FileSecondUploadService {

    @Db
    private FileMapper fileMapper;

    @Inject
    private FileService fileService;

    /**
     * 尝试秒传（查找相同identifier和存储源的文件）
     * 
     * @param identifier            文件唯一标识（MD5）
     * @param userId                用户ID
     * @param targetStorageSourceId 目标存储源ID
     * @return 找到可复用的文件返回FileDO，否则返回null
     */
    public FileDO trySecondUpload(String identifier, Long userId, Long targetStorageSourceId) {
        if (identifier == null || targetStorageSourceId == null) {
            return null;
        }

        // 查找相同identifier和存储源的文件
        var lambdaQuery = new LambdaQueryChainWrapper<>(fileMapper);
        var existingFile = lambdaQuery
                .eq(FileDO::getIdentifier, identifier)
                .eq(FileDO::getStorageSourceId, targetStorageSourceId)
                .one();

        if (existingFile != null) {
            log.info("文件秒传成功: identifier={}, storageSourceId={}", identifier, targetStorageSourceId);
            return existingFile;
        }

        // 如果同存储源没有，查找其他存储源的文件（需要复制）
        var otherStorageFile = fileService.getFileByIdentifier(identifier);
        if (otherStorageFile != null && !Objects.equals(otherStorageFile.getStorageSourceId(), targetStorageSourceId)) {
            log.info("文件存在于其他存储源，需要复制: identifier={}, sourceStorage={}, targetStorage={}",
                    identifier, otherStorageFile.getStorageSourceId(), targetStorageSourceId);
            // 返回null，由调用方决定是否复制
            return null;
        }

        return null;
    }

    /**
     * 根据identifier查找文件（任意存储源）
     * 
     * @param identifier 文件唯一标识
     * @return 找到返回FileDO，否则返回null
     */
    public FileDO findByIdentifier(String identifier) {
        return fileService.getFileByIdentifier(identifier);
    }

    /**
     * 检查用户是否已有相同文件（考虑存储源）
     * 
     * @param identifier            文件标识
     * @param userId                用户ID
     * @param parentId              父目录ID
     * @param fileName              文件名
     * @param targetStorageSourceId 目标存储源ID
     * @return 是否存在
     */
    public boolean hasExistingFile(String identifier, Long userId, Long parentId,
            String fileName, Long targetStorageSourceId) {
        var existingFile = trySecondUpload(identifier, userId, targetStorageSourceId);
        return existingFile != null;
    }
}