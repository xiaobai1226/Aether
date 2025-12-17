package com.xiaobai1226.aether.core.service.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.enums.CategoryEnum;
import com.xiaobai1226.aether.core.infrastructure.storage.StorageBackendFactory;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
import java.util.Set;

/**
 * 彻底删除文件（物理对象 + FileDO 记录）服务
 *
 * 说明：这是“轻量一致性套路”的一部分：数据库引用先断开/删除，物理对象删除失败不影响接口结果，后续可通过定时任务或人工清理。
 */
@Component
@Slf4j
public class FilePurgeService {

    @Db
    private FileMapper fileMapper;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageBackendFactory storageBackendFactory;

    @Inject("${project.path.root}")
    private String rootPath;

    public void purgeFiles(Long userId, Set<Long> fileIds) {
        if (CollUtil.isEmpty(fileIds)) {
            return;
        }

        List<FileDO> delFileList = ChainWrappers.lambdaQueryChain(fileMapper)
                .in(FileDO::getId, fileIds)
                .list();
        if (CollUtil.isEmpty(delFileList)) {
            return;
        }

        int delFileCount = fileMapper.deleteBatchIds(fileIds);
        if (delFileCount != fileIds.size()) {
            log.warn("彻底删除 FileDO 数量不一致，期望={}, 实际={}", fileIds.size(), delFileCount);
        }

        for (var delFile : delFileList) {
            // 1) 删除缩略图（缩略图统一在 rootPath 下）
            try {
                if (delFile.getThumbnail() != null
                        && (CategoryEnum.isPictureBySuffix(delFile.getSuffix())
                        || CategoryEnum.isVideoBySuffix(delFile.getSuffix()))) {
                    var thumbnailFilePath = com.xiaobai1226.aether.common.util.FileUtils.generatePath(
                            rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL, delFile.getThumbnail());
                    if (FileUtil.exist(thumbnailFilePath)) {
                        FileUtil.del(thumbnailFilePath);
                    }
                }
            } catch (Exception e) {
                log.warn("删除缩略图失败: thumbnail={}", delFile.getThumbnail(), e);
            }

            // 2) 删除物理文件（通过 StorageBackend）
            try {
                var storageSourceId = delFile.getStorageSourceId();
                var storageSource = storageSourceId != null
                        ? storageSourceService.getStorageSourceById(storageSourceId, userId)
                        : storageSourceService.getDefaultStorageSource(userId);
                if (storageSource == null) {
                    log.warn("无法获取存储源，跳过物理删除: fileId={}, path={}", delFile.getId(), delFile.getPath());
                    continue;
                }

                var backend = storageBackendFactory.getByType(storageSource.getType());
                backend.delete(storageSource.getPath(), delFile.getPath());
            } catch (Exception e) {
                log.warn("删除物理文件失败（可后续清理）: fileId={}, path={}", delFile.getId(), delFile.getPath(), e);
            }
        }
    }
}