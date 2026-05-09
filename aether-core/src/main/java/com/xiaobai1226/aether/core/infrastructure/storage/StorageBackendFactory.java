package com.xiaobai1226.aether.core.infrastructure.storage;

import com.xiaobai1226.aether.common.enums.StorageSourceTypeEnum;
import com.xiaobai1226.aether.common.exception.FailResultException;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;

/**
 * 存储后端工厂（Factory，工厂模式）
 */
@Component
public class StorageBackendFactory {

    @Inject
    private LocalFsBackend localFsBackend;

    public StorageBackend getByType(Integer storageSourceType) {
        var typeEnum = StorageSourceTypeEnum.getByType(storageSourceType);
        if (typeEnum == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, "未知存储源类型");
        }

        if (typeEnum == StorageSourceTypeEnum.LOCAL) {
            return localFsBackend;
        }

        // 你计划支持对象存储：后续在此处扩展 S3/OSS/COS/MinIO 等后端实现
        throw new FailResultException(BAD_REQUEST_ERROR, "暂不支持该存储源类型: " + typeEnum.name());
    }
}