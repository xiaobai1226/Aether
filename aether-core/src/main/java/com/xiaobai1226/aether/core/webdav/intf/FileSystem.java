package com.xiaobai1226.aether.core.webdav.intf;

import java.io.InputStream;
import java.util.List;

/**
 * webdav文件系统
 *
 * @author 高压锅里的小白
 */
public interface FileSystem {
    FileInfo fileInfo(String reqPath, Integer userId);

    String fileMime(FileInfo fi);

    List<FileInfo> fileList(String reqPath, Integer userId);

    String findEtag(String reqPath, FileInfo fi);

    InputStream fileInputStream(String reqPath, long start, long length, Integer userId);

    boolean putFile(String reqPath, InputStream in, Integer userId);

    boolean del(String reqPath);

    boolean copy(String reqPath, String descPath);

    boolean move(String reqPath, String descPath);

    boolean mkdir(String reqPath);

    String fileUrl(String reqPath);
}