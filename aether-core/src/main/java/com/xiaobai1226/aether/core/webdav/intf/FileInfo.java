package com.xiaobai1226.aether.core.webdav.intf;

/**
 * webdav文件属性
 *
 * @author 高压锅里的小白
 */
public interface FileInfo {
    /**
     * 文件(夹)名
     *
     * @return
     */
    String name();

    /**
     * 是否是文件夹
     *
     * @return
     */
    boolean isDir();

    /**
     * 大小
     *
     * @return
     */
    long size();

    /**
     * 文件路径
     *
     * @return
     */
    String path();

    /**
     * 更新时间
     * 请使用yyyy-MM-dd HH:mm:ss格式
     *
     * @return
     */
    String update();

    /**
     * 创建时间
     * 请使用yyyy-MM-dd HH:mm:ss格式
     *
     * @return
     */
    String create();
}
