package com.xiaobai1226.aether.core.webdav.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.core.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.util.FileUtils;
import com.xiaobai1226.aether.core.webdav.intf.FileInfo;
import com.xiaobai1226.aether.core.webdav.intf.FileSystem;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.Utils;
import org.noear.solon.web.webdav.impl.ShardingInputStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 网盘文件系统
 */
@Component
public class NetdiskFileSystem implements FileSystem {

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private UserFileService userFileService;

    @Override
    public FileInfo fileInfo(String reqPath, Integer userId) {
        if (Objects.equals(reqPath, "")) {
            return new FileInfo() {
                @Override
                public String name() {
                    return "";
                }

                @Override
                public boolean isDir() {
                    return true;
                }

                @Override
                public long size() {
                    return 0;
                }

                @Override
                public String path() {
                    return "";
                }

                @Override
                public String update() {
                    return new Date().toString();
                }

                @Override
                public String create() {
                    return new Date().toString();
                }
            };
        }
        var userFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);

        return this.userFileDTO2Info(userFileDTO);
    }

    @Override
    public String fileMime(FileInfo fi) {
        return fi.isDir() ? "httpd/unix-directory" : "text/plain";
    }

    @Override
    public List<FileInfo> fileList(String reqPath, Integer userId) {

        var parentId = 0;
        if (StrUtil.isNotEmpty(reqPath)) {
            var parentUserFile = userFileService.getParentFolderByPath(userId, parentId, reqPath);
            if (parentUserFile == null) {
                return null;
            }
            parentId = parentUserFile.getId();
        }

        var userFileDTOListPage = userFileService.getFileList(userId, parentId, null);
        if (userFileDTOListPage == null || CollUtil.isEmpty(userFileDTOListPage.getList())) {
            return null;
        }

        List<FileInfo> list = new ArrayList<>();
        for (var userFileDTO : userFileDTOListPage.getList()) {
            FileInfo fi = this.userFileDTO2Info(userFileDTO);
            if (fi != null) {
                list.add(fi);
            }
        }
        return list;
    }

    @Override
    public String findEtag(String reqPath, FileInfo fi) {
        return "W/\"" + Utils.md5(fi.update() + reqPath) + "\"";
    }

    @Override
    public InputStream fileInputStream(String reqPath, long start, long length, Integer userId) {
        var userFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
        var fileFullPath = FileUtils.generatePath(rootPath, userFileDTO.getPath());
        InputStream in = FileUtil.getInputStream(fileFullPath);
        if (length == 0) {
            return in;
        } else {
            return new ShardingInputStream(in, start, length);
        }
    }

    @Override
    public boolean putFile(String reqPath, InputStream in, Integer userId) {
        return false;
    }

    @Override
    public boolean del(String reqPath) {
        return false;
    }

    @Override
    public boolean copy(String reqPath, String descPath) {
        return false;
    }

    @Override
    public boolean move(String reqPath, String descPath) {
        return false;
    }

    @Override
    public boolean mkdir(String reqPath) {
        return false;
    }

    @Override
    public String fileUrl(String reqPath) {
        return null;
    }

    private FileInfo userFileDTO2Info(UserFileDTO userFileDTO) {
        if (userFileDTO == null) {
            return null;
        }
        return new FileInfo() {
            @Override
            public String name() {
                return userFileDTO.getName();
            }

            @Override
            public boolean isDir() {
                return UserFileItemTypeEnum.isFolder(userFileDTO.getItemType());
            }

            @Override
            public long size() {
                return UserFileItemTypeEnum.isFolder(userFileDTO.getItemType()) ? 0 : userFileDTO.getSize();
            }

            @Override
            public String path() {
                return "";
            }

            @Override
            public String update() {
                return userFileDTO.getUpdateTime();
            }

            @Override
            public String create() {
                return userFileDTO.getCreateTime();
            }
        };
    }
}
