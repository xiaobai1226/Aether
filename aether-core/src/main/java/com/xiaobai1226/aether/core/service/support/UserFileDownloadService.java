package com.xiaobai1226.aether.core.service.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;

import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.infrastructure.storage.StorageBackendFactory;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.DownloadedFile;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_NO_STORAGE_SOURCE;

/**
 * 下载服务（从 UserFileServiceImpl 中抽离）
 */
@Component
@Slf4j
public class UserFileDownloadService {

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageBackendFactory storageBackendFactory;

    public DownloadedFile download(List<UserFileTreeDTO> userFileTreeDTOList, Long userId)
            throws IOException {
        if (userFileTreeDTOList.size() == 1
                && UserFileItemTypeEnum.isFile(userFileTreeDTOList.getFirst().getItemType())) {
            var node = userFileTreeDTOList.getFirst();
            var storageSource = storageSourceService.getStorageSourceById(node.getStorageSourceId(), userId);
            if (storageSource == null) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
            }

            var backend = storageBackendFactory.getByType(storageSource.getType());
            var absPath = backend.tryResolveAbsolutePath(storageSource.getPath(), node.getPath());
            if (absPath != null) {
                return new DownloadedFile(new File(absPath), node.getName());
            }
            return new DownloadedFile("application/octet-stream",
                    backend.openStream(storageSource.getPath(), node.getPath()), node.getName());
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                zipFiles(userFileTreeDTOList, zos, null, true, userId);
            }

            var name = "打包下载.zip";
            if (userFileTreeDTOList.size() == 1) {
                name = userFileTreeDTOList.getFirst().getName() + "." + "zip";
            }

            return new DownloadedFile("application/zip", new ByteArrayInputStream(baos.toByteArray()), name);
        }
    }

    private void zipFiles(List<UserFileTreeDTO> userFileTreeDTOList, ZipOutputStream zipOutputStream,
            String parentPath, Boolean isRoot, Long userId) {
        for (var userFileTreeDTO : userFileTreeDTOList) {
            var fileFullPath = parentPath == null ? userFileTreeDTO.getName()
                    : FileUtils.generatePath(parentPath, userFileTreeDTO.getName());

            try {
                if (UserFileItemTypeEnum.isFolder(userFileTreeDTO.getItemType())) {
                    if (isRoot && userFileTreeDTOList.size() == 1) {
                        fileFullPath = null;
                    } else {
                        ZipEntry zipEntry = new ZipEntry(fileFullPath + "/");
                        zipOutputStream.putNextEntry(zipEntry);
                    }

                    if (CollUtil.isNotEmpty(userFileTreeDTO.getChildUserFileDTOList())) {
                        zipFiles(userFileTreeDTO.getChildUserFileDTOList(), zipOutputStream, fileFullPath, false,
                                userId);
                    }
                } else {
                    ZipEntry zipEntry = new ZipEntry(fileFullPath);
                    zipOutputStream.putNextEntry(zipEntry);

                    var storageSource = storageSourceService.getStorageSourceById(userFileTreeDTO.getStorageSourceId(),
                            userId);
                    if (storageSource == null) {
                        throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
                    }

                    var backend = storageBackendFactory.getByType(storageSource.getType());
                    try (InputStream in = backend.openStream(storageSource.getPath(), userFileTreeDTO.getPath())) {
                        IoUtil.copy(in, zipOutputStream);
                    }
                }

                zipOutputStream.closeEntry();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}