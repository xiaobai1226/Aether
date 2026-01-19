package com.xiaobai1226.aether.core.webdav.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;

import com.xiaobai1226.aether.core.application.FileOperationsFacade;
import com.xiaobai1226.aether.core.domain.vo.CopyAndRenameVO;
import com.xiaobai1226.aether.core.domain.vo.CopyVO;
import com.xiaobai1226.aether.core.domain.vo.DeleteVO;
import com.xiaobai1226.aether.core.domain.vo.NewFolderVO;
import com.xiaobai1226.aether.core.domain.vo.UserFileVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.infrastructure.storage.StorageBackendFactory;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.webdav.UserContext;
import com.xiaobai1226.aether.core.webdav.WebDavPathAdapter;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.Utils;
import org.noear.solon.web.webdav.FileInfo;
import org.noear.solon.web.webdav.FileSystem;
import org.noear.solon.web.webdav.impl.ShardingInputStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 网盘文件系统
 * 
 * 重构说明：使用 WebDavPathAdapter 适配器统一调用标准业务逻辑，
 * 复用完整的 UseCase（业务校验、存储源迁移等），避免代码重复和功能漂移
 */
@Component
@Slf4j
public class NetdiskFileSystem implements FileSystem {

    @Inject
    private UserFileService userFileService;

    @Inject
    private WebDavPathAdapter pathAdapter;

    @Inject
    private FileOperationsFacade fileOperationsFacade;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageBackendFactory storageBackendFactory;

    @Inject
    private FileService fileService;

    /**
     * 获取文件/文件夹信息
     * 
     * 对应的 WebDAV HTTP 方法：PROPFIND
     * 
     * 作用：根据请求路径获取文件或文件夹的详细信息，包括名称、大小、修改时间、创建时间等。
     * 
     * 调用场景：
     * 客户端查看文件属性（名称、大小、修改时间）
     * 检查文件是否存在
     * Windows 资源管理器显示文件详情
     * macOS Finder 显示文件信息
     * 
     * 特殊处理：
     * 当 reqPath 为空字符串时，返回根目录的 FileInfo（isDir=true）
     * 其他情况通过 userFileService 查询用户文件信息
     * 
     * @param reqPath 请求路径，相对于 WebDAV 根目录（例如："/文件夹/文件.txt" 或 "" 表示根目录）
     * @return FileInfo 对象，包含文件/文件夹的详细信息；如果文件不存在返回 null
     */
    @Override
    public FileInfo fileInfo(String reqPath) {
        Long userId = UserContext.getUserId();
        log.info("WebDAV fileInfo 开始: reqPath={}, userId={}", reqPath, userId);
        long startTime = System.currentTimeMillis();
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

        log.info("WebDAV fileInfo 结束: 耗时={}ms, reqPath={}, userFileDTO={}", System.currentTimeMillis() - startTime,
                reqPath, userFileDTO);
        return this.userFileDTO2Info(userFileDTO);
    }

    /**
     * 获取文件 MIME 类型
     * 
     * 对应的 WebDAV HTTP 方法：PROPFIND（返回 getcontenttype 属性）
     * 
     * 作用：返回文件或文件夹的 MIME 类型，用于客户端正确显示文件图标或选择打开方式。
     * 
     * 调用场景：
     * 客户端需要知道文件类型以正确显示图标
     * 确定文件的打开方式
     * 在文件列表中显示文件类型标识
     * 
     * 返回值说明：
     * 文件夹：返回 "httpd/unix-directory"
     * 文件：根据文件扩展名返回对应的 MIME 类型（如 image/png、application/pdf 等）
     * 未知类型：返回 "application/octet-stream"（二进制流）
     * 
     * @param fi FileInfo 对象，包含文件/文件夹的基本信息
     * @return MIME 类型字符串
     */
    @Override
    public String fileMime(FileInfo fi) {
        log.info("WebDAV fileMime 开始: fi={}", fi);
        long startTime = System.currentTimeMillis();
        // 文件夹返回标准的目录类型
        if (fi.isDir()) {
            return "httpd/unix-directory";
        }

        // 根据文件名获取 MIME 类型
        String fileName = fi.name();
        if (StrUtil.isNotEmpty(fileName)) {
            String mimeType = FileUtil.getMimeType(fileName);
            // 如果识别到 MIME 类型则返回，否则返回默认的二进制流类型
            if (StrUtil.isNotEmpty(mimeType)) {
                log.info("WebDAV fileMime 结束: 耗时={}ms, fi={}", System.currentTimeMillis() - startTime, fi);
                return mimeType;
            }
        }
        log.info("WebDAV fileMime 结束: 耗时={}ms, fi={}", System.currentTimeMillis() - startTime, fi);

        // 默认返回二进制流类型（而不是 text/plain，避免浏览器错误解析二进制文件）
        return "application/octet-stream";
    }

    /**
     * 列出目录内容
     * 
     * 对应的 WebDAV HTTP 方法：PROPFIND（depth=1）
     * 
     * 作用：列出指定目录下的所有文件和子文件夹。
     * 
     * 调用场景：
     * 打开文件夹，显示里面的文件列表
     * macOS Finder 浏览 WebDAV 目录
     * Windows 资源管理器打开文件夹
     * WebDAV 客户端刷新目录内容
     * 
     * 处理逻辑：
     * 通过 FileOperationsFacade 复用 getFileListByPage() 方法
     * 内部包含 path → parentId 转换逻辑，避免代码重复
     * 将查询结果转换为 FileInfo 列表返回
     * 
     * @param reqPath 请求路径，相对于 WebDAV 根目录（例如："/文件夹" 或 "" 表示根目录）
     * @return FileInfo 列表，包含目录下所有文件和子文件夹的信息；如果目录不存在或为空返回 null
     */
    @Override
    public List<FileInfo> fileList(String reqPath) {
        Long userId = UserContext.getUserId();
        log.info("WebDAV fileList 开始: reqPath={}, userId={}", reqPath, userId);
        long startTime = System.currentTimeMillis();
        try {
            // 构造 UserFileVO，设置路径
            var userFileVO = new UserFileVO();
            userFileVO.setPath(reqPath);
            userFileVO.setPageNum(1);
            userFileVO.setPageSize(-1);

            // 复用 Facade 的 getFileListByPage()，内部已包含 path → parentId 转换逻辑
            var pageResult = fileOperationsFacade.getFileListByPage(userFileVO, userId);

            if (pageResult == null || CollUtil.isEmpty(pageResult.getList())) {
                log.info("WebDAV fileList 结束: 耗时={}ms, reqPath={}, userId={}", System.currentTimeMillis() - startTime,
                        reqPath, userId);
                return null;
            }

            // 转换为 FileInfo 列表
            List<FileInfo> list = new ArrayList<>();
            for (var userFileDTO : pageResult.getList()) {
                FileInfo fi = this.userFileDTO2Info(userFileDTO);
                if (fi != null) {
                    list.add(fi);
                }
            }
            log.info("WebDAV fileList 结束: 耗时={}ms, reqPath={}, userId={}", System.currentTimeMillis() - startTime,
                    reqPath, userId);
            return list;
        } catch (FailResultException e) {
            // WebDAV 的错误处理：返回 null 而不是抛出异常
            log.warn("fileList 失败: 耗时={}ms, reqPath={}, userId={}, error={}", System.currentTimeMillis() - startTime,
                    reqPath, userId, e.getMessage());
            return null;
        }
    }

    /**
     * 生成文件的 ETag（实体标签）
     * 
     * 对应的 WebDAV HTTP 方法：PROPFIND（返回 getetag 属性）
     * 
     * 作用：为文件生成唯一的 ETag 标识符，用于缓存控制和版本检测。
     * 
     * 调用场景：
     * 客户端使用 ETag 判断文件是否被修改
     * 避免重复下载未改动的文件（缓存优化）
     * 支持条件请求（If-None-Match、If-Match）
     * 
     * ETag 格式：
     * 格式：W/"MD5值"（W 表示弱验证器）
     * 基于文件的修改时间和路径生成 MD5 值
     * 文件内容或修改时间变化时，ETag 会改变
     * 
     * @param reqPath 请求路径
     * @param fi      FileInfo 对象，包含文件的修改时间等信息
     * @return ETag 字符串，格式为 W/"MD5值"
     */
    @Override
    public String findEtag(String reqPath, FileInfo fi) {
        log.info("WebDAV findEtag 开始: reqPath={}, fi={}", reqPath, fi);
        long startTime = System.currentTimeMillis();
        String etag = "W/\"" + Utils.md5(fi.update() + reqPath) + "\"";
        log.info("WebDAV findEtag 结束: 耗时={}ms, reqPath={}, fi={}, etag={}", System.currentTimeMillis() - startTime,
                reqPath, fi, etag);
        return etag;
    }

    /**
     * 读取文件内容（支持断点续传）
     * 
     * 对应的 WebDAV HTTP 方法：GET
     * 
     * 作用：读取文件的内容流，支持指定读取的起始位置和长度，用于文件下载和断点续传。
     * 
     * 调用场景：
     * 下载文件（完整下载）
     * 断点续传（使用 start 和 length 参数指定范围）
     * 在线预览文件（部分读取）
     * 视频/音频文件的流式播放
     * 
     * 参数说明：
     * start：读取的起始字节位置（从 0 开始）
     * length：要读取的字节长度
     * length=0：表示读取从 start 位置到文件末尾的所有内容
     * 
     * 处理逻辑：
     * 1. 根据路径获取用户文件信息
     * 2. 获取文件实体信息（包含存储源ID）
     * 3. 根据存储源ID获取对应的存储源配置
     * 4. 使用 StorageBackend 打开文件流（支持多存储源）
     * 5. 如果需要范围读取，使用 ShardingInputStream 包装
     * 
     * @param reqPath 请求路径，相对于 WebDAV 根目录
     * @param start   读取的起始字节位置
     * @param length  要读取的字节长度，0 表示读取到文件末尾
     * @return InputStream 输入流，包含文件内容或指定范围的内容
     */
    @Override
    public InputStream fileInputStream(String reqPath, long start, long length) {
        log.info("WebDAV fileInputStream 开始: reqPath={}, start={}, length={}", reqPath, start, length);
        try {
            Long userId = UserContext.getUserId();

            // 1. 获取用户文件信息
            var userFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
            if (userFileDTO == null) {
                throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            }

            // 2. 获取文件实体信息
            // var fileDO = fileService.getFileById(userFileDTO.getFileId());
            // if (fileDO == null) {
            // throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
            // }

            // 3. 获取文件对应的存储源
            var storageSource = storageSourceService.getStorageSourceById(userFileDTO.getStorageSourceId(), userId);
            if (storageSource == null) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
            }

            // 4. 使用 StorageBackend 打开文件流
            var backend = storageBackendFactory.getByType(storageSource.getType());
            InputStream in = backend.openStream(storageSource.getPath(), userFileDTO.getPath());

            // 5. 根据需要包装为范围读取流
            if (length == 0) {
                return in;
            } else {
                return new ShardingInputStream(in, start, length);
            }
        } catch (Exception e) {
            log.error("WebDAV fileInputStream 失败: reqPath={}", reqPath, e);
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 上传文件（覆盖已存在的文件）
     * 
     * 对应的 WebDAV HTTP 方法：PUT
     * 
     * 作用：将文件内容写入到指定路径，如果文件已存在则覆盖。
     * 
     * 调用场景：
     * 上传新文件到 WebDAV 服务器
     * 覆盖已存在的文件（编辑保存）
     * 拖拽文件到 WebDAV 文件夹
     * 复制文件到 WebDAV 服务器
     * 
     * 实现说明：
     * 通过 WebDavPathAdapter 适配器调用，复用完整的业务逻辑
     * 包含业务校验、存储源迁移、配额检查等功能
     * 确保与标准文件上传接口的行为一致
     * 
     * @param reqPath 请求路径，相对于 WebDAV 根目录（例如："/文件夹/新文件.txt"）
     * @param in      文件内容的输入流
     * @return true 表示上传成功，false 表示失败
     */
    @Override
    public boolean putFile(String reqPath, InputStream in) {
        Long userId = UserContext.getUserId();
        log.info("WebDAV putFile 开始: reqPath={}, userId={}", reqPath, userId);
        try {
            if (StrUtil.isEmpty(reqPath) || in == null) {
                log.warn("WebDAV putFile 参数无效: reqPath={}, in={}, userId={}", reqPath, (in == null ? "null" : "exists"),
                        userId);
                return false;
            }

            log.info("WebDAV putFile 开始: reqPath={}, userId={}", reqPath, userId);
            // 直接调用 Facade 的 putFileByPath 方法处理上传
            // putFileByPath 内部已处理：路径解析、同名文件检查、覆盖逻辑（原子性删除+创建）
            boolean result = fileOperationsFacade.putFileByPath(reqPath, in, userId);
            log.info("WebDAV putFile 完成: reqPath={}, userId={}, result={}", reqPath, userId, result);
            return result;
        } catch (Exception e) {
            log.error("WebDAV putFile 失败: reqPath={}, userId={}", reqPath, userId, e);
            return false;
        }
    }

    /**
     * 删除文件或文件夹
     * 
     * 对应的 WebDAV HTTP 方法：DELETE
     * 
     * 作用：删除指定路径的文件或文件夹（包括非空文件夹）。
     * 
     * 调用场景：
     * 删除文件（右键删除或按 Delete 键）
     * 删除空文件夹
     * 递归删除非空文件夹及其所有内容
     * 
     * 实现说明：
     * 通过 WebDavPathAdapter 适配器调用，复用 DeleteFileUseCase 的完整业务逻辑
     * 包含权限校验、回收站处理等业务规则
     * 确保与标准文件删除接口的行为一致
     * 
     * @param reqPath 请求路径，相对于 WebDAV 根目录
     * @return true 表示删除成功，false 表示失败
     */
    @Override
    public boolean del(String reqPath) {
        Long userId = UserContext.getUserId();
        log.info("WebDAV del 开始: reqPath={}, userId={}", reqPath, userId);
        if (StrUtil.isEmpty(reqPath)) {
            return false;
        }

        // 1. 路径 → 文件信息
        UserFileDTO userFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
        if (userFileDTO == null) {
            return false;
        }

        // 2. 构建 DeleteVO
        DeleteVO deleteVO = new DeleteVO();
        deleteVO.setIds(List.of(userFileDTO.getId()));

        // 3. 调用标准 delete 方法（复用 UseCase 业务逻辑）
        fileOperationsFacade.deleteToRecycle(deleteVO, userId);
        return true;
    }

    /**
     * 复制文件或文件夹
     * 
     * 对应的 WebDAV HTTP 方法：COPY
     * 
     * 作用：将文件或文件夹复制到目标路径，原文件保持不变。
     * 
     * 调用场景：
     * 复制文件到另一个位置（Ctrl+C、Ctrl+V）
     * 复制文件夹及其所有内容
     * 创建文件的副本
     * 
     * 实现说明：
     * 直接复用 CopyFileUseCase 的完整业务逻辑（包含校验、配额检查等）
     * 包含路径校验、配额检查、权限验证等功能
     * 确保与标准文件复制接口的行为一致
     * 
     * 功能限制：
     * 仅支持跨目录复制（保持文件名不变），不支持复制并重命名
     * 如果目标文件名与源文件名不同，操作将失败
     * 
     * @param reqPath  源文件/文件夹路径，相对于 WebDAV 根目录
     * @param descPath 目标路径，相对于 WebDAV 根目录
     * @return true 表示复制成功，false 表示失败
     */
    @Override
    public boolean copy(String reqPath, String descPath) {
        Long userId = UserContext.getUserId();
        log.info("WebDAV copy 开始: reqPath={}, descPath={}, userId={}", reqPath, descPath, userId);
        if (StrUtil.isEmpty(reqPath) || StrUtil.isEmpty(descPath)) {
            log.warn("WebDAV copy 参数无效: reqPath={}, descPath={}, userId={}", reqPath, descPath, userId);
            return false;
        }

        try {
            log.info("WebDAV copy 开始: reqPath={}, descPath={}, userId={}", reqPath, descPath, userId);

            // 1. 源路径 → 文件信息
            UserFileDTO sourceFileDTO = userFileService.getUserFileDTOByPath(userId, reqPath);
            if (sourceFileDTO == null) {
                log.warn("WebDAV copy 源文件不存在: reqPath={}, userId={}", reqPath, userId);
                return false;
            }

            // 2. 解析目标路径（提取父路径和文件名）
            int lastSlashIndex = descPath.lastIndexOf("/");
            String parentPath = "";
            String fileName = descPath;

            if (lastSlashIndex > 0) {
                parentPath = descPath.substring(0, lastSlashIndex);
                fileName = descPath.substring(lastSlashIndex + 1);
            } else if (lastSlashIndex == 0) {
                parentPath = "";
                fileName = descPath.substring(1);
            }

            log.debug("WebDAV copy 路径解析: parentPath={}, fileName={}, sourceFileName={}",
                    parentPath, fileName, sourceFileDTO.getName());

            // 3. 判断是否需要重命名
            if (!sourceFileDTO.getName().equals(fileName)) {
                // 需要重命名：调用 copyAndRename
                log.info("WebDAV copy 执行重命名复制: sourceId={}, targetPath={}, newName={}",
                        sourceFileDTO.getId(), parentPath, fileName);
                CopyAndRenameVO copyAndRenameVO = new CopyAndRenameVO();
                copyAndRenameVO.setSourceId(sourceFileDTO.getId());
                copyAndRenameVO.setTargetPath(parentPath);
                copyAndRenameVO.setNewName(fileName);
                fileOperationsFacade.copyAndRename(copyAndRenameVO, userId);
            } else {
                // 不需要重命名：调用普通 copy
                log.info("WebDAV copy 执行普通复制: sourceId={}, targetPath={}",
                        sourceFileDTO.getId(), parentPath);
                CopyVO copyVO = new CopyVO();
                copyVO.setSourceIds(List.of(sourceFileDTO.getId()));
                copyVO.setTargetPath(parentPath);
                fileOperationsFacade.copy(copyVO, userId);
            }

            log.info("WebDAV copy 成功: reqPath={}, descPath={}, userId={}", reqPath, descPath, userId);
            return true;
        } catch (Exception e) {
            log.error("WebDAV copy 失败: reqPath={}, descPath={}, userId={}", reqPath, descPath, userId, e);
            return false;
        }
    }

    /**
     * 移动文件或文件夹（也可用于重命名）
     * 
     * 对应的 WebDAV HTTP 方法：MOVE
     * 
     * 作用：将文件或文件夹移动到目标路径，原位置的文件会被删除。如果源路径和目标路径在同一目录，则实现重命名功能。
     * 
     * 调用场景：
     * 移动文件到另一个文件夹（拖拽操作）
     * 重命名文件（移动到相同目录但不同文件名）
     * 移动文件夹及其所有内容
     * 
     * 实现说明：
     * 通过 WebDavPathAdapter 适配器调用，复用 MoveFileUseCase 的完整业务逻辑
     * 包含存储源迁移、路径校验、权限验证等功能
     * 确保与标准文件移动接口的行为一致
     * 
     * @param reqPath  源文件/文件夹路径，相对于 WebDAV 根目录
     * @param descPath 目标路径，相对于 WebDAV 根目录
     * @return true 表示移动成功，false 表示失败
     */
    @Override
    public boolean move(String reqPath, String descPath) {
        Long userId = UserContext.getUserId();
        log.info("WebDAV move 开始: reqPath={}, descPath={}, userId={}", reqPath, descPath, userId);
        // 通过适配器调用，复用 MoveFileUseCase 的完整业务逻辑（包含存储源迁移、校验等）
        return pathAdapter.adaptMove(reqPath, descPath, userId);
    }

    /**
     * 创建文件夹
     * 
     * 对应的 WebDAV HTTP 方法：MKCOL（Make Collection）
     * 
     * 作用：在指定路径创建新的文件夹（目录）。
     * 
     * 调用场景：
     * 新建文件夹（右键 → 新建文件夹）
     * 创建多级目录结构
     * 客户端需要创建目录时
     * 
     * 实现说明：
     * 解析 WebDAV 完整路径为 folderName 和 parentPath，
     * 直接调用 FileOperationsFacade.newFolder，复用 CreateFolderUseCase 的完整业务逻辑
     * 包含路径校验、权限验证、重名检查等功能
     * 确保与标准文件夹创建接口的行为一致
     * 
     * @param reqPath 要创建的文件夹路径，相对于 WebDAV 根目录（例如："/工作/项目/新文件夹"）
     * @return true 表示创建成功，false 表示失败
     */
    @Override
    public boolean mkdir(String reqPath) {
        Long userId = UserContext.getUserId();
        log.info("WebDAV mkdir 开始: reqPath={}, userId={}", reqPath, userId);
        try {
            if (Utils.isEmpty(reqPath)) {
                return false;
            }

            // 解析路径：提取父路径和新文件夹名称
            // 例如："/工作/项目/新文件夹" -> parentPath="/工作/项目", folderName="新文件夹"
            int lastSlashIndex = reqPath.lastIndexOf("/");
            String parentPath = "";
            String folderName = reqPath;

            if (lastSlashIndex > 0) {
                // 中间路径，如 "/工作/项目/新文件夹"
                parentPath = reqPath.substring(0, lastSlashIndex);
                folderName = reqPath.substring(lastSlashIndex + 1);
            } else if (lastSlashIndex == 0) {
                // 根目录下，如 "/新文件夹"
                parentPath = "";
                folderName = reqPath.substring(1);
            }

            // 构建 NewFolderVO 并调用标准业务方法
            NewFolderVO newFolderVO = new NewFolderVO();
            newFolderVO.setFolderName(folderName);
            newFolderVO.setPath(parentPath);

            // 直接调用 Facade 方法，复用 CreateFolderUseCase 业务逻辑
            fileOperationsFacade.newFolder(newFolderVO, userId);
            return true;
        } catch (Exception e) {
            log.error("WebDAV 创建文件夹失败: reqPath={}", reqPath, e);
            return false;
        }
    }

    /**
     * 获取文件的直接访问 URL
     * 
     * 作用：返回文件的直接访问 URL，用于某些特殊场景下的文件访问。
     * 
     * 调用场景：
     * 某些 WebDAV 客户端可能需要文件的直接下载链接
     * 用于生成文件的分享链接（如果支持）
     * 
     * 当前实现：
     * 返回 null，表示不支持直接 URL 访问
     * 文件访问统一通过 WebDAV 协议进行
     * 
     * @param reqPath 请求路径，相对于 WebDAV 根目录
     * @return 文件的直接访问 URL，如果不支持则返回 null
     */
    @Override
    public String fileUrl(String reqPath) {
        log.info("WebDAV fileUrl 开始: reqPath={}", reqPath);
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