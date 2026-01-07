package com.xiaobai1226.aether.core.infrastructure.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 存储后端接口（Strategy，策略模式）
 *
 * 面向多存储源（本地/对象存储等）的最小抽象。业务层只关心 basePath（存储源根）与 key（相对路径/对象key）。
 */
public interface StorageBackend {

    /**
     * 判断对象是否存在
     *
     * @param basePath 存储源根路径（或 bucket 等）
     * @param key      相对路径/对象key
     */
    boolean exists(String basePath, String key);

    /**
     * 打开对象输入流
     *
     * @param basePath 存储源根路径（或 bucket 等）
     * @param key      相对路径/对象key
     */
    InputStream openStream(String basePath, String key) throws IOException;

    /**
     * 将本地文件写入存储
     *
     * @param basePath   存储源根路径（或 bucket 等）
     * @param key        相对路径/对象key
     * @param sourceFile 本地源文件
     * @param overwrite  是否覆盖
     */
    void putFile(String basePath, String key, File sourceFile, boolean overwrite) throws IOException;

    /**
     * 在同/跨存储源复制对象
     */
    void copy(String sourceBasePath, String sourceKey, String targetBasePath, String targetKey, boolean overwrite)
            throws IOException;

    /**
     * 删除对象
     */
    void delete(String basePath, String key) throws IOException;

    /**
     * 尝试解析为本地绝对路径（仅本地存储有意义，对象存储可返回 null）
     */
    String tryResolveAbsolutePath(String basePath, String key);
}