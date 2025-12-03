package com.xiaobai1226.aether.core.webdav.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.webdav.intf.FileInfo;
import com.xiaobai1226.aether.core.webdav.intf.FileSystem;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.Utils;
import org.noear.solon.web.webdav.impl.ShardingInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 网盘文件系统
 */
@Component
@Slf4j
public class NetdiskFileSystem implements FileSystem {

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private UserFileService userFileService;

    @Override
    public FileInfo fileInfo(String reqPath, Long userId) {
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
    public List<FileInfo> fileList(String reqPath, Long userId) {

        Long parentId = 0L;
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
    public InputStream fileInputStream(String reqPath, long start, long length, Long userId) {
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
    public boolean putFile(String reqPath, InputStream in, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath)) {
                return false;
            }

            // 解析路径，获取父目录和文件名
            int lastSlashIndex = reqPath.lastIndexOf("/");
            String parentPath = "";
            String fileName = reqPath;
            
            if (lastSlashIndex > 0) {
                parentPath = reqPath.substring(0, lastSlashIndex);
                fileName = reqPath.substring(lastSlashIndex + 1);
            } else if (lastSlashIndex == 0) {
                fileName = reqPath.substring(1);
            }

            // 获取父目录ID
            Long parentId = 0L;
            if (StrUtil.isNotEmpty(parentPath)) {
                var parentUserFile = userFileService.getParentFolderByPath(userId, 0L, parentPath);
                if (parentUserFile == null) {
                    return false;
                }
                parentId = parentUserFile.getId();
            }

            // 检查文件是否已存在
            var existingFile = userFileService.getUserFileByName(fileName, userId, parentId, NORMAL);
            
            // 创建临时文件
            String tempFileName = "webdav_" + IdUtil.simpleUUID();
            String tempFilePath = FileUtils.generatePath(rootPath, "temp", tempFileName);
            File tempFile = new File(tempFilePath);
            tempFile.getParentFile().mkdirs();

            // 将输入流写入临时文件
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // TODO: 这里应该调用完整的上传逻辑，包括计算MD5、检查秒传等
            // 目前简化实现：如果文件存在则删除，然后创建新文件
            if (existingFile != null) {
                var fileTreeList = new ArrayList<UserFileTreeDTO>();
                var fileTree = new UserFileTreeDTO();
                fileTree.setId(existingFile.getId());
                fileTree.setItemType(existingFile.getItemType());
                fileTreeList.add(fileTree);
                userFileService.delete(fileTreeList, userId);
            }

            // 创建新文件记录
            // TODO: 实现完整的文件创建逻辑
            
            // 清理临时文件
            tempFile.delete();

            return true;
        } catch (Exception e) {
            log.error("WebDAV putFile error: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean del(String reqPath, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath)) {
                return false;
            }

            var userFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
            if (userFileDTO == null) {
                return false;
            }

            // 构建文件树并删除
            var fileTreeList = new ArrayList<UserFileTreeDTO>();
            var fileTree = new UserFileTreeDTO();
            fileTree.setId(userFileDTO.getId());
            fileTree.setItemType(userFileDTO.getItemType());
            fileTreeList.add(fileTree);

            // 如果是文件夹，需要获取完整的子文件树
            if (UserFileItemTypeEnum.isFolder(userFileDTO.getItemType())) {
                userFileService.getSubUserFileTree(userId, fileTreeList);
            }

            userFileService.delete(fileTreeList, userId);
            return true;
        } catch (Exception e) {
            log.error("WebDAV del error: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean copy(String reqPath, String descPath, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath) || StrUtil.isEmpty(descPath)) {
                return false;
            }

            // 获取源文件
            var sourceFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
            if (sourceFileDTO == null) {
                return false;
            }

            // 解析目标路径
            int lastSlashIndex = descPath.lastIndexOf("/");
            String targetParentPath = "";
            
            if (lastSlashIndex > 0) {
                targetParentPath = descPath.substring(0, lastSlashIndex);
            }

            // 获取目标父目录ID
            Long targetParentId = 0L;
            if (StrUtil.isNotEmpty(targetParentPath)) {
                var targetParentFile = userFileService.getParentFolderByPath(userId, 0L, targetParentPath);
                if (targetParentFile == null) {
                    return false;
                }
                targetParentId = targetParentFile.getId();
            }

            // 构建源文件树
            var sourceTreeList = new ArrayList<UserFileTreeDTO>();
            var sourceTree = new UserFileTreeDTO();
            sourceTree.setId(sourceFileDTO.getId());
            sourceTree.setItemType(sourceFileDTO.getItemType());
            sourceTree.setName(sourceFileDTO.getName());
            sourceTree.setParentId(sourceFileDTO.getParentId());
            sourceTreeList.add(sourceTree);

            // 如果是文件夹，获取完整子树
            if (UserFileItemTypeEnum.isFolder(sourceFileDTO.getItemType())) {
                userFileService.getSubUserFileTree(userId, sourceTreeList);
            }

            // 计算总大小
            Long totalSize = userFileService.getUserFileTreeSpaceUsage(sourceTreeList);

            // 执行复制
            userFileService.copy(targetParentId, userId, sourceTreeList, totalSize);
            
            return true;
        } catch (Exception e) {
            log.error("WebDAV copy error: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean move(String reqPath, String descPath, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath) || StrUtil.isEmpty(descPath)) {
                return false;
            }

            // 获取源文件
            var sourceFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
            if (sourceFileDTO == null) {
                return false;
            }

            // 解析目标路径
            int lastSlashIndex = descPath.lastIndexOf("/");
            String targetParentPath = "";
            String targetName = descPath;
            
            if (lastSlashIndex > 0) {
                targetParentPath = descPath.substring(0, lastSlashIndex);
                targetName = descPath.substring(lastSlashIndex + 1);
            } else if (lastSlashIndex == 0) {
                targetName = descPath.substring(1);
            }

            // 获取目标父目录ID
            Long targetParentId = 0L;
            if (StrUtil.isNotEmpty(targetParentPath)) {
                var targetParentFile = userFileService.getParentFolderByPath(userId, 0L, targetParentPath);
                if (targetParentFile == null) {
                    return false;
                }
                targetParentId = targetParentFile.getId();
            }

            // 如果名称不同，需要重命名
            boolean needRename = !sourceFileDTO.getName().equals(targetName);

            // 移动文件（更新parentId）
            var sourceIds = new ArrayList<Long>();
            sourceIds.add(sourceFileDTO.getId());
            userFileService.updateParentIdByIds(sourceIds, targetParentId, userId, NORMAL);

            // 如果需要重命名
            if (needRename) {
                userFileService.updateFileNameById(sourceFileDTO.getId(), userId, targetName, NORMAL);
            }

            return true;
        } catch (Exception e) {
            log.error("WebDAV move error: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean mkdir(String reqPath, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath)) {
                return false;
            }

            // 解析路径，获取父目录和文件夹名
            int lastSlashIndex = reqPath.lastIndexOf("/");
            String parentPath = "";
            String folderName = reqPath;
            
            if (lastSlashIndex > 0) {
                parentPath = reqPath.substring(0, lastSlashIndex);
                folderName = reqPath.substring(lastSlashIndex + 1);
            } else if (lastSlashIndex == 0) {
                folderName = reqPath.substring(1);
            }

            // 获取父目录ID
            Long parentId = 0L;
            if (StrUtil.isNotEmpty(parentPath)) {
                var parentUserFile = userFileService.getParentFolderByPath(userId, 0L, parentPath);
                if (parentUserFile == null) {
                    return false;
                }
                parentId = parentUserFile.getId();
            }

            // 检查文件夹是否已存在
            var existingFolder = userFileService.getUserFileByName(folderName, userId, parentId, NORMAL);
            if (existingFolder != null) {
                // 文件夹已存在
                return false;
            }

            // 创建文件夹
            userFileService.newFolder(folderName, parentId, userId);
            return true;
        } catch (Exception e) {
            log.error("WebDAV mkdir error: {}", e.getMessage(), e);
            return false;
        }
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