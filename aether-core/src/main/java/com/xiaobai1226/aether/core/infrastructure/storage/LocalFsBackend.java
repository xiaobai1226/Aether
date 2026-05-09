package com.xiaobai1226.aether.core.infrastructure.storage;

import cn.hutool.core.io.FileUtil;
import com.xiaobai1226.aether.common.util.FileUtils;
import org.noear.solon.annotation.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 本地文件系统存储后端
 */
@Component
public class LocalFsBackend implements StorageBackend {

    @Override
    public boolean exists(String basePath, String key) {
        if (basePath == null || key == null) {
            return false;
        }
        return FileUtil.exist(FileUtils.generatePath(basePath, key));
    }

    @Override
    public InputStream openStream(String basePath, String key) throws IOException {
        return FileUtil.getInputStream(FileUtils.generatePath(basePath, key));
    }

    @Override
    public void putFile(String basePath, String key, File sourceFile, boolean overwrite) throws IOException {
        var targetPath = FileUtils.generatePath(basePath, key);
        var targetFile = FileUtil.file(targetPath);
        FileUtil.mkParentDirs(targetFile);
        if (!overwrite && targetFile.exists()) {
            return;
        }
        // 使用 copy，避免跨盘 move 失败；需要原子/一致性时由上层决定策略
        FileUtil.copy(sourceFile, targetFile, overwrite);
    }

    @Override
    public void copy(String sourceBasePath, String sourceKey, String targetBasePath, String targetKey,
            boolean overwrite) throws IOException {
        var sourcePath = FileUtils.generatePath(sourceBasePath, sourceKey);
        var targetPath = FileUtils.generatePath(targetBasePath, targetKey);
        var targetFile = FileUtil.file(targetPath);
        FileUtil.mkParentDirs(targetFile);
        FileUtil.copy(sourcePath, targetPath, overwrite);
    }

    @Override
    public void delete(String basePath, String key) throws IOException {
        FileUtil.del(FileUtils.generatePath(basePath, key));
    }

    @Override
    public String tryResolveAbsolutePath(String basePath, String key) {
        if (basePath == null || key == null) {
            return null;
        }
        return FileUtils.generatePath(basePath, key);
    }
}