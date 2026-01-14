package com.xiaobai1226.aether.core.webdav;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路径信息
 * 用于存储解析后的路径和文件名
 *
 * @author 高压锅里的小白
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PathInfo {
    /**
     * 父路径（不包含文件名）
     * 例如："/folder/subfolder" 或 "" （根目录）
     */
    private String parentPath;

    /**
     * 文件名或文件夹名
     * 例如："file.txt" 或 "newfolder"
     */
    private String fileName;
}