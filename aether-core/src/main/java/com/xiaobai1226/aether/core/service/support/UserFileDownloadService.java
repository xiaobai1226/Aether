package com.xiaobai1226.aether.core.service.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;

import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.domain.dto.DownloadLocalFileDTO;
import com.xiaobai1226.aether.core.domain.dto.DownloadPackageFileDTO;
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
    private static final String DOWNLOAD_ZIP_TEMP_DIR = "download";


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
            var localFileDTO = resolveSingleLocalFile(node, userId);
            if (localFileDTO != null) {
                return new DownloadedFile(localFileDTO.getFile(), localFileDTO.getFileName());
            }
            var storageSource = storageSourceService.getStorageSourceById(node.getStorageSourceId(), userId);
            var backend = storageBackendFactory.getByType(storageSource.getType());
            return new DownloadedFile("application/octet-stream",
                    backend.openStream(storageSource.getPath(), node.getPath()), node.getName());
        } else {
            var packageFile = buildPackageFile(userFileTreeDTOList, userId);
            return new DownloadedFile("application/zip",
                    new DeleteOnCloseFileInputStream(packageFile.getFile()), packageFile.getFileName());
        }
    }

    public DownloadPackageFileDTO buildPackageFile(List<UserFileTreeDTO> userFileTreeDTOList, Long userId)
            throws IOException {
        return buildPackageFile(userFileTreeDTOList, userId, null);
    }

    public DownloadPackageFileDTO buildPackageFile(List<UserFileTreeDTO> userFileTreeDTOList, Long userId,
            ZipProgressListener progressListener) throws IOException {
        File tempZipFile = createTempZipFile();
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tempZipFile)))) {
            zipFiles(userFileTreeDTOList, zos, null, true, userId, progressListener);
        } catch (IOException e) {
            FileUtil.del(tempZipFile);
            throw e;
        }
        return new DownloadPackageFileDTO(tempZipFile, getZipFileName(userFileTreeDTOList));
    }

    public DownloadLocalFileDTO resolveSingleLocalFile(UserFileTreeDTO node, Long userId) {
        var storageSource = storageSourceService.getStorageSourceById(node.getStorageSourceId(), userId);
        if (storageSource == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
        }

        var backend = storageBackendFactory.getByType(storageSource.getType());
        var absPath = backend.tryResolveAbsolutePath(storageSource.getPath(), node.getPath());
        if (absPath == null) {
            return null;
        }
        return new DownloadLocalFileDTO(new File(absPath), node.getName());
    }

    private void zipFiles(List<UserFileTreeDTO> userFileTreeDTOList, ZipOutputStream zipOutputStream,
            String parentPath, Boolean isRoot, Long userId, ZipProgressListener progressListener) throws IOException {
        for (var userFileTreeDTO : userFileTreeDTOList) {
            var fileFullPath = parentPath == null ? userFileTreeDTO.getName()
                    : FileUtils.generatePath(parentPath, userFileTreeDTO.getName());

            if (UserFileItemTypeEnum.isFolder(userFileTreeDTO.getItemType())) {
                if (!(isRoot && userFileTreeDTOList.size() == 1)) {
                    ZipEntry zipEntry = new ZipEntry(fileFullPath + "/");
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.closeEntry();
                }

                if (CollUtil.isNotEmpty(userFileTreeDTO.getChildUserFileDTOList())) {
                    zipFiles(userFileTreeDTO.getChildUserFileDTOList(), zipOutputStream, fileFullPath, false,
                            userId, progressListener);
                }
                continue;
            }

            ZipEntry zipEntry = new ZipEntry(fileFullPath);
            zipOutputStream.putNextEntry(zipEntry);

            var storageSource = storageSourceService.getStorageSourceById(userFileTreeDTO.getStorageSourceId(),
                    userId);
            if (storageSource == null) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
            }

            var backend = storageBackendFactory.getByType(storageSource.getType());
            try (InputStream in = backend.openStream(storageSource.getPath(), userFileTreeDTO.getPath())) {
                long copiedBytes = IoUtil.copy(in, zipOutputStream);
                if (progressListener != null) {
                    progressListener.onFilePacked(copiedBytes);
                }
            } finally {
                zipOutputStream.closeEntry();
            }
        }
    }

    private File createTempZipFile() throws IOException {
        var tempDirPath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_TEMP_FILE_FULL, DOWNLOAD_ZIP_TEMP_DIR);
        FileUtil.mkdir(tempDirPath);
        var tempFile = File.createTempFile("download-", ".zip", new File(tempDirPath));
        tempFile.deleteOnExit();
        return tempFile;
    }

    private String getZipFileName(List<UserFileTreeDTO> userFileTreeDTOList) {
        if (userFileTreeDTOList.size() == 1) {
            return userFileTreeDTOList.getFirst().getName() + ".zip";
        }
        return "打包下载.zip";
    }

    private static final class DeleteOnCloseFileInputStream extends FileInputStream {
        private final File tempFile;
        private boolean closed;

        private DeleteOnCloseFileInputStream(File tempFile) throws FileNotFoundException {
            super(tempFile);
            this.tempFile = tempFile;
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            try {
                super.close();
            } finally {
                if (tempFile.exists() && !FileUtil.del(tempFile)) {
                    log.warn("下载临时压缩文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    @FunctionalInterface
    public interface ZipProgressListener {
        void onFilePacked(long packedBytes);
    }
}