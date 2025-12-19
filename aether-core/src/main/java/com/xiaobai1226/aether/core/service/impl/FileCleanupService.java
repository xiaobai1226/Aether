package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xiaobai1226.aether.core.service.support.FilePurgeService;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文件清理服务
 * 
 * 职责：查找和清理无引用的文件
 * 
 * @author bai
 */
@Component
@Slf4j
public class FileCleanupService {

    @Db
    private FileMapper fileMapper;

    @Db
    private UserFileMapper userFileMapper;

    @Inject
    private FilePurgeService filePurgeService;

    /**
     * 查找无引用的文件
     * 
     * @return 无引用的文件ID列表
     */
    public List<Long> findOrphanFiles() {
        try {
            // 1. 获取所有File记录的ID
            var lambdaQuery = new LambdaQueryChainWrapper<>(fileMapper);
            var allFiles = lambdaQuery
                    .select(FileDO::getId)
                    .last("LIMIT 10000")
                    .list(); // 每次最多处理10000个

            if (CollUtil.isEmpty(allFiles)) {
                return new ArrayList<>();
            }

            Set<Long> allFileIds = new HashSet<>();
            for (FileDO file : allFiles) {
                allFileIds.add(file.getId());
            }

            log.debug("共有{}个File记录", allFileIds.size());

            // 2. 获取所有UserFile引用的fileId
            var referencedFiles = userFileMapper.selectList(new LambdaQueryChainWrapper<>(userFileMapper)
                    .select(UserFileDO::getFileId)
                    .isNotNull(UserFileDO::getFileId));

            Set<Long> referencedFileIds = new HashSet<>();
            for (var userFile : referencedFiles) {
                if (userFile.getFileId() != null) {
                    referencedFileIds.add(userFile.getFileId());
                }
            }

            log.debug("共有{}个File被引用", referencedFileIds.size());

            // 3. 找出无引用的文件
            List<Long> orphanFileIds = new ArrayList<>();
            for (Long fileId : allFileIds) {
                if (!referencedFileIds.contains(fileId)) {
                    orphanFileIds.add(fileId);
                }
            }

            log.info("发现{}个无引用文件", orphanFileIds.size());
            return orphanFileIds;

        } catch (Exception e) {
            log.error("查找无引用文件失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 批量清理文件（物理文件+数据库记录）
     * 
     * 这是系统级清理任务，不需要关联特定用户
     * 
     * @param fileIds 要清理的文件ID列表
     */
    @Tran
    public void cleanupFiles(List<Long> fileIds) {
        if (CollUtil.isEmpty(fileIds)) {
            return;
        }

        log.info("系统级清理任务：开始清理{}个无引用文件", fileIds.size());

        try {
            filePurgeService.purgeFilesSystem(new HashSet<>(fileIds));

            log.info("系统级清理任务：文件清理完成，共清理{}个文件", fileIds.size());

        } catch (Exception e) {
            log.error("系统级清理任务：清理文件失败", e);
            throw e;
        }
    }

    /**
     * 获取文件引用计数
     * 
     * @param fileId 文件ID
     * @return 引用计数
     */
    public int getReferenceCount(Long fileId) {
        if (fileId == null) {
            return 0;
        }

        return userFileMapper.selectCount(new LambdaQueryChainWrapper<>(userFileMapper)
                .eq(UserFileDO::getFileId, fileId))
                .intValue();
    }
}