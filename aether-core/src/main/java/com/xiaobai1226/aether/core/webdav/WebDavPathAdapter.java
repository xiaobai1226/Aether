package com.xiaobai1226.aether.core.webdav;

import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.core.application.FileOperationsFacade;
import com.xiaobai1226.aether.core.domain.vo.*;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * WebDAV 路径适配器
 * 
 * 职责：将 WebDAV 协议的路径参数转换为标准 Facade 方法所需的参数，
 * 使 WebDAV 请求可以复用完整的 UseCase 业务逻辑（校验、存储源迁移等）
 *
 * @author 高压锅里的小白
 */
@Component
@Slf4j
public class WebDavPathAdapter {

    @Inject
    private UserFileService userFileService;

    @Inject
    private FileOperationsFacade facade;

    /**
     * 适配 WebDAV MOVE 操作
     * 
     * @param reqPath  源文件路径
     * @param descPath 目标路径
     * @param userId   用户ID
     * @return 是否成功
     */
    public boolean adaptMove(String reqPath, String descPath, Long userId) {
        try {
            if (StrUtil.isEmpty(reqPath) || StrUtil.isEmpty(descPath)) {
                return false;
            }

            // 1. 源路径 → 文件信息
            UserFileDTO sourceFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
            if (sourceFileDTO == null) {
                return false;
            }

            // 2. 解析目标路径
            PathInfo pathInfo = parsePath(descPath);

            // 3. 判断是否需要重命名
            boolean needRename = !sourceFileDTO.getName().equals(pathInfo.getFileName());

            // 4. 获取源文件的父路径（用于判断是否只是重命名）
            String sourceParentPath = getParentPath(reqPath);

            // 5. 如果父路径相同且需要重命名，只执行重命名操作
            if (sourceParentPath.equals(pathInfo.getParentPath()) && needRename) {
                FileRenameVO renameVO = new FileRenameVO();
                renameVO.setId(sourceFileDTO.getId());
                renameVO.setNewName(pathInfo.getFileName());
                facade.rename(renameVO, userId);
                return true;
            }

            // 6. 如果父路径不同，执行移动操作
            if (!sourceParentPath.equals(pathInfo.getParentPath())) {
                MoveVO moveVO = new MoveVO();
                List<Long> sourceIds = new ArrayList<>();
                sourceIds.add(sourceFileDTO.getId());
                moveVO.setSourceIds(sourceIds);
                moveVO.setTargetPath(pathInfo.getParentPath());
                facade.move(moveVO, userId);
            }

            // 7. 如果需要重命名，在移动后执行重命名
            if (needRename && !sourceParentPath.equals(pathInfo.getParentPath())) {
                FileRenameVO renameVO = new FileRenameVO();
                renameVO.setId(sourceFileDTO.getId());
                renameVO.setNewName(pathInfo.getFileName());
                facade.rename(renameVO, userId);
            }

            return true;
        } catch (Exception e) {
            log.error("WebDAV adaptMove 失败: reqPath={}, descPath={}", reqPath, descPath, e);
            return false;
        }
    }

    /**
     * 解析路径，返回父路径和文件名
     * 
     * @param path 完整路径（如 "/folder/subfolder/file.txt"）
     * @return PathInfo 对象
     */
    public PathInfo parsePath(String path) {
        if (StrUtil.isEmpty(path)) {
            return new PathInfo("", "");
        }

        int lastSlashIndex = path.lastIndexOf("/");
        String parentPath = "";
        String fileName = path;

        if (lastSlashIndex > 0) {
            parentPath = path.substring(0, lastSlashIndex);
            fileName = path.substring(lastSlashIndex + 1);
        } else if (lastSlashIndex == 0) {
            // 根目录下的文件，如 "/file.txt"
            parentPath = "";
            fileName = path.substring(1);
        }

        return new PathInfo(parentPath, fileName);
    }

    /**
     * 获取父路径（不包含文件名）
     * 
     * @param path 完整路径
     * @return 父路径
     */
    public String getParentPath(String path) {
        if (StrUtil.isEmpty(path)) {
            return "";
        }

        int lastSlashIndex = path.lastIndexOf("/");
        if (lastSlashIndex > 0) {
            return path.substring(0, lastSlashIndex);
        }
        return "";
    }
}