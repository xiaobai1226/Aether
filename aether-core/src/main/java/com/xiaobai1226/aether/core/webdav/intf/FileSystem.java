package com.xiaobai1226.aether.core.webdav.intf;

import java.io.InputStream;
import java.util.List;

/**
 * webdav文件系统
 *
 * @author 高压锅里的小白
 */
public interface FileSystem {
    FileInfo fileInfo(String reqPath, Long userId);

    String fileMime(FileInfo fi);

    List<FileInfo> fileList(String reqPath, Long userId);

    String findEtag(String reqPath, FileInfo fi);

    InputStream fileInputStream(String reqPath, long start, long length, Long userId);

    boolean putFile(String reqPath, InputStream in, Long userId);

    boolean del(String reqPath, Long userId);

    boolean copy(String reqPath, String descPath, Long userId);

    boolean move(String reqPath, String descPath, Long userId);

    boolean mkdir(String reqPath, Long userId);

    String fileUrl(String reqPath);
}