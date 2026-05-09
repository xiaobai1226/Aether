package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobai1226.aether.common.enums.CategoryEnum;
import com.xiaobai1226.aether.core.cache.FileCache;
import com.xiaobai1226.aether.core.cache.UserCache;
import com.xiaobai1226.aether.core.domain.dto.*;
import com.xiaobai1226.aether.core.domain.vo.UserFileVO;
import com.xiaobai1226.aether.core.domain.vo.UserFolderVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.enums.UserFileStatusEnum;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.FileService;
import com.xiaobai1226.aether.core.service.intf.QuotaService;
import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.util.LockManager;
import com.xiaobai1226.aether.core.infrastructure.storage.StorageBackendFactory;
import com.xiaobai1226.aether.core.service.support.UserFileDownloadService;
import com.xiaobai1226.aether.core.service.support.UserFileTreeService;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
import com.xiaobai1226.aether.dao.domain.entity.RecycleBinDO;
import com.xiaobai1226.aether.dao.domain.entity.StorageSourceDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Cache;

import java.util.Objects;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.data.annotation.Tran;

import java.io.*;
import java.util.*;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.CategoryEnum.OTHER;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 用户文件service实现类
 *
 * @author bai
 */
@Component
@Slf4j
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFileDO> implements UserFileService {

    @Db
    private UserFileMapper userFileMapper;

    @Db
    private FileMapper fileMapper;

    @Inject
    private FileCache fileCache;

    @Inject
    private UserCache userCache;

    @Inject
    private UserService userService;

    @Inject
    private QuotaService quotaService;

    @Inject
    private FileService fileService;

    @Inject
    private RecycleBinService recycleBinService;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private StorageBackendFactory storageBackendFactory;

    @Inject
    private UserFileTreeService userFileTreeService;

    @Inject
    private UserFileDownloadService userFileDownloadService;

    @Inject
    private StorageMigrationService storageMigrationService;

    @Override
    public UserFileDO getParentFolderByPath(final Long userId, Long parentId, String path) {
        if (StrUtil.isEmpty(path)) {
            return null;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] dirs = path.split("/");

        if (dirs.length == 0) {
            return null;
        }

        if (parentId == null) {
            parentId = 0L;
        }

        LambdaQueryChainWrapper<UserFileDO> lambdaQuery;
        UserFileDO userFileDO;
        for (int i = 0; i < dirs.length; i++) {
            lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
            userFileDO = lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId)
                    .eq(UserFileDO::getName, dirs[i]).eq(UserFileDO::getFileStatus, NORMAL.flag())
                    .eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag()).one();

            if (userFileDO == null) {
                break;
            } else if (i == dirs.length - 1) {
                return userFileDO;
            } else {
                parentId = userFileDO.getId();
            }
        }

        return null;
    }

    @Override
    public UserFileDO getParentFolderByPathOrCreate(final Long userId, UserFolderDTO parentFolder, String path) {
        if (StrUtil.isEmpty(path)) {
            return null;
        }
        if (parentFolder == null) {
            return null;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] dirs = path.split("/");

        if (dirs.length == 0) {
            return null;
        }

        LambdaQueryChainWrapper<UserFileDO> lambdaQuery;
        UserFileDO currentFolder = parentFolder;

        // 是否新建
        var isCreate = false;
        var lockKey = "";
        for (int i = 0; i < dirs.length; i++) {
            UserFileDO userFileDO;
            lockKey = currentFolder.getId() + ":" + dirs[i];

            // 上锁
            LockManager.lock(lockKey);

            try {
                if (!isCreate) {
                    lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
                    userFileDO = lambdaQuery.eq(UserFileDO::getUserId, userId)
                            .eq(UserFileDO::getParentId, currentFolder.getId())
                            .eq(UserFileDO::getName, dirs[i]).eq(UserFileDO::getFileStatus, NORMAL.flag())
                            .eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag()).one();

                    if (userFileDO == null) {
                        var userFolderDTO = BeanUtil.copyProperties(currentFolder, UserFolderDTO.class);
                        userFileDO = newFolder(dirs[i], userFolderDTO, userId);
                        isCreate = true;
                    }
                } else {
                    var userFolderDTO = BeanUtil.copyProperties(currentFolder, UserFolderDTO.class);
                    userFileDO = newFolder(dirs[i], userFolderDTO, userId);
                }
                currentFolder = userFileDO;
            } finally {
                LockManager.unlock(lockKey);
            }

            if (i == dirs.length - 1) {
                return currentFolder;
            }
        }

        return null;
    }

    /**
     * 根据路径获取用户文件信息
     * 使用 Solon 缓存注解，key 包含 userId 和 path 实现用户隔离
     * 注意：此缓存是通用的，不仅服务于 WebDAV，也能被 HTTP API 复用
     */
    @Cache(
        key = "userFile:byPath:${userId}:${path}", 
        tags = "userFile:user:${userId}",
        seconds = 60
    )
    @Override
    public UserFileDTO getUserFileDTOByPath(final Long userId, String path) {
        if (StrUtil.isEmpty(path)) {
            return null;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] dirs = path.split("/");

        if (dirs.length == 0) {
            return null;
        }

        var parentId = 0L;

        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(NORMAL.flag());
        for (int i = 0; i < dirs.length; i++) {
            userFileDO.setParentId(parentId).setName(dirs[i]);

            // 查询文件列表
            var userFileDTO = userFileMapper.getUserFileDTOByNameAndParentId(userFileDO);

            if (userFileDTO == null) {
                break;
            } else if (i == dirs.length - 1) {
                return userFileDTO;
            } else {
                parentId = userFileDTO.getId();
            }
        }

        return null;
    }

    @Override
    public PageResult<UserFileDTO> getFileList(final Long userId, Long parentId, UserFileVO userFileVO) {
        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(NORMAL.flag());

        if (userFileVO == null) {
            userFileVO = new UserFileVO();
            userFileVO.setPageNum(1);
            userFileVO.setPageSize(-1);
        }

        Set<String> suffixSet = null;
        // 如果分类为全部分类，则不设置分类条件
        if (userFileVO.getCategory() != null) {
            if (userFileVO.getCategory().equals(OTHER.id())) {
                suffixSet = CategoryEnum.getAllSuffix();
            } else {
                suffixSet = CategoryEnum.getSuffixSet(userFileVO.getCategory());
            }
            userFileDO.setItemType(FILE.flag());
        } else {
            userFileDO.setParentId(parentId);
        }

        // 分页对象
        Page<UserFileDTO> page = new Page<>(userFileVO.getPageNum(), userFileVO.getPageSize());

        // 查询文件列表
        var userFileDTOList = userFileMapper.getFileListByPage(page, userFileDO, userFileVO.getCategory(), suffixSet,
                userFileVO.getSortingField(), userFileVO.getSortingMethod());

        // 判断结果是否为空
        if (CollUtil.isNotEmpty(userFileDTOList)) {
            page.setRecords(userFileDTOList);
            return new PageResult<>(page);
        }

        return null;
    }

    @Override
    // public UserFileDO getUserFileByName(String fileName, Integer userId, Integer
    // parentId, UserFileStatusEnum userFileStatus, UserFileItemTypeEnum itemType) {
    public UserFileDO getUserFileByName(String fileName, final Long userId, Long parentId,
            UserFileStatusEnum userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        // return lambdaQuery.eq(UserFileDO::getUserId,
        // userId).eq(UserFileDO::getParentId, parentId).eq(UserFileDO::getName,
        // fileName).eq(UserFileDO::getFileStatus,
        // userFileStatus.flag()).eq(UserFileDO::getItemType, itemType.flag()).one();
        return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId)
                .eq(UserFileDO::getName, fileName).eq(UserFileDO::getFileStatus, userFileStatus.flag()).one();
    }

    @Override
    public UserFileDO newFolder(String folderName, UserFolderDTO parentFolder, final Long userId) {
        return addUserFile(userId, null, parentFolder.getId(), folderName, UserFileItemTypeEnum.FOLDER, NORMAL, null,
                parentFolder.getStorageSourceId());
    }

    @Override
    public UserFileDO getUserFileByIdAndUserId(Long id, final Long userId, UserFileStatusEnum userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getId, id);
        if (userFileStatus != null) {
            lambdaQuery.eq(UserFileDO::getFileStatus, userFileStatus.flag());
        }
        return lambdaQuery.one();
    }

    @Override
    public Boolean updateFileNameById(Long id, final Long userId, String newName, UserFileStatusEnum userFileStatus) {
        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFileDO::getName, newName).eq(UserFileDO::getId, id)
                .eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatus.flag());
        var updateNameResult = userFileMapper.update(null, lambdaUpdateWrapper);

        return updateNameResult == 1;
    }

    @Override
    public Boolean rename(Long id, final Long userId, String newName, UserFileDO userFileDO,
            UserFileStatusEnum userFileStatus) {
        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFileDO::getName, newName).eq(UserFileDO::getId, id)
                .eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatus.flag());

        if (UserFileItemTypeEnum.isFile(userFileDO.getItemType())) {
            var suffix = FileNameUtil.extName(newName);
            if (StrUtil.isNotEmpty(suffix)) {
                lambdaUpdateWrapper.set(UserFileDO::getSuffix, suffix.toLowerCase());
            } else {
                lambdaUpdateWrapper.set(UserFileDO::getSuffix, null);
            }
        }

        var updateNameResult = userFileMapper.update(null, lambdaUpdateWrapper);

        return updateNameResult == 1;
    }

    @Override
    public PageResult<UserFileDO> getFolderList(final Long userId, Long parentId, UserFolderVO userFolderVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        var userFolderListPage = lambdaQuery.eq(UserFileDO::getUserId, userId)
                .eq(UserFileDO::getItemType, UserFileItemTypeEnum.FOLDER.flag())
                .eq(UserFileDO::getFileStatus, NORMAL.flag()).eq(UserFileDO::getParentId, parentId)
                .orderByDesc(UserFileDO::getUpdateTime)
                .page(new Page<>(userFolderVO.getPageNum(), userFolderVO.getPageSize()));

        // 判断结果是否为空
        if (CollUtil.isNotEmpty(userFolderListPage.getRecords())) {
            return new PageResult<>(userFolderListPage);
        }

        return null;
    }

    @Override
    public List<UserFileDO> getUserFileByIdsAndUserId(List<Long> ids, final Long userId,
            UserFileStatusEnum userFileStatus) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        return lambdaQuery.eq(UserFileDO::getUserId, userId).in(UserFileDO::getId, ids)
                .eq(UserFileDO::getFileStatus, userFileStatus.flag()).list();
    }

    @Override
    public List<Long> getAllSubfolders(final Long userId, List<Long> ids) {

        var totalSubfolderIds = new ArrayList<Long>();

        var userFolderVO = new UserFolderVO();
        userFolderVO.setPageNum(1);
        // 小于0 表示不分页
        userFolderVO.setPageSize(-1);
        for (var id : ids) {
            var subfoldersPage = getFolderList(userId, id, userFolderVO);

            if (subfoldersPage == null || CollUtil.isEmpty(subfoldersPage.getList())) {
                continue;
            }

            var tempSubfolderIds = new ArrayList<Long>();
            for (var subfolder : subfoldersPage.getList()) {
                totalSubfolderIds.add(subfolder.getId());
                tempSubfolderIds.add(subfolder.getId());
            }

            var tempTotalSubfolderIds = getAllSubfolders(userId, tempSubfolderIds);

            if (CollUtil.isNotEmpty(tempTotalSubfolderIds)) {
                totalSubfolderIds.addAll(tempTotalSubfolderIds);
            }
        }

        return totalSubfolderIds;
    }

    @Override
    public Long getCountByNames(List<String> fileNames, final Long userId, Long parentId,
            UserFileStatusEnum userFileStatus, UserFileItemTypeEnum itemType) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        return lambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getParentId, parentId)
                .in(UserFileDO::getName, fileNames).eq(UserFileDO::getFileStatus, userFileStatus.flag())
                .eq(UserFileDO::getItemType, itemType.flag()).count();
    }

    @Tran
    @Override
    public void updateParentIdByIds(List<Long> sourceIds, Long targetId, final Long userId,
            UserFileStatusEnum userFileStatus) {
        LambdaUpdateWrapper<UserFileDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFileDO::getParentId, targetId).in(UserFileDO::getId, sourceIds)
                .eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, userFileStatus.flag());
        var updateNameCount = userFileMapper.update(null, lambdaUpdateWrapper);

        if (updateNameCount != sourceIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    @Override
    public List<UserFileDTO> getUserFileDTOListByIds(List<Long> ids, final Long userId,
            UserFileStatusEnum userFileStatus) {
        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(userFileStatus.flag());
        var userFileDTOList = userFileMapper.getUserFileDTOByIds(userFileDO, ids);
        if (CollUtil.isEmpty(userFileDTOList)) {
            return null;
        }

        return userFileDTOList;
    }

    @Override
    public List<UserFileTreeDTO> getUserFileTreeListByIds(List<Long> ids, final Long userId,
            UserFileStatusEnum userFileStatus) {
        var userFileDTOList = getUserFileDTOListByIds(ids, userId, userFileStatus);
        if (CollUtil.isEmpty(userFileDTOList)) {
            return null;
        }

        return BeanUtil.copyToList(userFileDTOList, UserFileTreeDTO.class);
    }

    @Override
    public void getSubUserFileTree(final Long userId, List<UserFileTreeDTO> userFileTreeList) {
        userFileTreeService.fillSubTree(userId, userFileTreeList);
    }

    @Override
    public Long getUserFileTreeSpaceUsage(List<UserFileTreeDTO> userFileTreeDTOList) {
        return userFileTreeService.calcSpaceUsage(userFileTreeDTOList);
    }

    @Tran
    @Override
    public void copy(UserFolderDTO targetFolder, final Long userId, List<UserFileTreeDTO> sourceUserFileTreeDTOList,
            Long totalSize) {
        // TODO 存储空间
        if (totalSize > 0) {
            // 预占上传空间（整个文件大小）
            // quotaService.reserveUploading(userId, totalSize);
        }

        insertUserFileTree(sourceUserFileTreeDTOList, userId, targetFolder);

        // TODO 更新用户所使用的空间
        // if (totalSize > 0) {
        // // 更新用户已使用存储空间
        // quotaService.increaseUsed(userId, totalSize);
        // // 释放上传预占（整个文件大小）
        // quotaService.releaseUploading(userId, totalSize);
        // }
    }

    @Tran
    @Override
    public void copyWithNewName(UserFolderDTO targetFolder, Long userId,
            UserFileTreeDTO sourceFileTree, String newName, Long totalSize) {
        // TODO 存储空间
        if (totalSize > 0) {
            // 预占上传空间（整个文件大小）
            // quotaService.reserveUploading(userId, totalSize);
        }

        // 修改源文件树的名称为新名称（只修改根节点）
        sourceFileTree.setName(newName);

        // 复用现有的 insertUserFileTree 方法
        insertUserFileTree(List.of(sourceFileTree), userId, targetFolder);

        // TODO 更新用户所使用的空间
        // if (totalSize > 0) {
        // // 更新用户已使用存储空间
        // quotaService.increaseUsed(userId, totalSize);
        // // 释放上传预占（整个文件大小）
        // quotaService.releaseUploading(userId, totalSize);
        // }
    }

    @Tran
    @Override
    public void delete(List<UserFileTreeDTO> delUserFileTreeList, final Long userId) {
        var delIds = new ArrayList<Long>();
        var recycleBinDOList = new ArrayList<RecycleBinDO>();

        // 获取全部删除信息
        getDelInfo(delUserFileTreeList, delIds, recycleBinDOList, userId, null, 1);

        // 修改文件状态为回收站
        updateUserFileStatusById(delIds, userId, UserFileStatusEnum.DEL);

        // 新增回收站数据
        var insertBatchResult = recycleBinService.insertBatch(recycleBinDOList);

        if (!insertBatchResult) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // TODO 定好回收站是否占用用户空间后，调整用户空间修改逻辑
    }

    @Tran
    @Override
    public void updateUserFileStatusById(List<Long> ids, final Long userId, UserFileStatusEnum userFileStatus) {
        var lambdaUpdateWrapper = new LambdaUpdateWrapper<UserFileDO>();
        lambdaUpdateWrapper.set(UserFileDO::getFileStatus, userFileStatus.flag()).eq(UserFileDO::getUserId, userId)
                .in(UserFileDO::getId, ids);
        var updateFileStatusCount = userFileMapper.update(null, lambdaUpdateWrapper);

        if (ids.size() != updateFileStatusCount) {
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 新增一条UserFile到数据库
     *
     * @param userId             用户ID
     * @param fileId             文件ID
     * @param parentId           父ID
     * @param fileName           文件名称
     * @param userFileItemType   条目类型
     * @param userFileStatusEnum 文件状态
     * @param fileSize           文件大小
     * @param storageSourceId    存储源ID
     */
    @Tran
    private UserFileDO addUserFile(Long userId, Long fileId, Long parentId, String fileName,
            UserFileItemTypeEnum userFileItemType, UserFileStatusEnum userFileStatusEnum, Long fileSize,
            Long storageSourceId) {

        var userFileDO = new UserFileDO();

        userFileDO.setUserId(userId);
        userFileDO.setItemType(userFileItemType.flag());
        userFileDO.setFileStatus(userFileStatusEnum.flag());
        userFileDO.setParentId(parentId);
        userFileDO.setStorageSourceId(storageSourceId);
        // 新建文件和文件夹默认使用继承类型
        userFileDO.setStorageSourceType(1);

        // 根据文件名称获取文件
        // var sameNameUserFile = getUserFileByName(fileName, userId,
        // userFileDO.getParentId(), userFileStatusEnum, userFileItemType);
        var sameNameUserFile = getUserFileByName(fileName, userId, userFileDO.getParentId(), userFileStatusEnum);
        // 如果不为null，则说明同名文件已存在，进行改名
        if (sameNameUserFile != null) {
            // 重命名文件名称
            fileName = FileUtils.rename(fileName);
        }
        userFileDO.setName(fileName);

        if (UserFileItemTypeEnum.isFile(userFileItemType)) {
            userFileDO.setFileId(fileId);

            var suffix = FileNameUtil.extName(fileName);
            if (StrUtil.isNotEmpty(suffix)) {
                userFileDO.setSuffix(suffix.toLowerCase());
            }
        }

        var resultCount = userFileMapper.insert(userFileDO);

        if (resultCount != 1) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // TODO 存储空间
        // if (UserFileItemTypeEnum.isFile(userFileItemType)) {
        // // 更新用户已使用存储空间（集中到 QuotaService，避免多流程漏改）
        // quotaService.increaseUsed(userId, fileSize == null ? 0L : fileSize);
        // }

        return userFileDO;
    }

    @Override
    public Long getStorageSourceIdByParent(UserFileDO parentUserFile, Long userId) {
        StorageSourceDO storageSource = null;

        // 如果是根目录，使用默认存储源
        if (parentUserFile == null) {
            storageSource = storageSourceService.getDefaultStorageSource(userId);
            if (storageSource == null) {
                throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
            }

            return storageSource.getId();
        }

        // 优化：如果传入的是 UserFolderDTO 且已包含存储源信息，直接返回，避免重复查询
        if (parentUserFile instanceof UserFolderDTO folder && folder.getStorageSource() != null) {
            return folder.getStorageSource().getId();
        }

        if (parentUserFile.getStorageSourceId() == null) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_NO_STORAGE_SOURCE);
        }

        return parentUserFile.getStorageSourceId();
    }

    @Override
    public UserFolderDTO getFolderDTO(Long userId, String path) {

        UserFolderDTO userFolderDTO = null;

        // 如果path为空，返回根目录
        if (StrUtil.isEmpty(path) || path.equals("/")) {
            userFolderDTO = UserFolderDTO.createRoot(userId);
        } else {
            // 获取文件夹
            UserFileDO userFile = getParentFolderByPath(userId, 0L, path);
            if (userFile != null) {
                userFolderDTO = BeanUtil.copyProperties(userFile, UserFolderDTO.class);
            }
        }

        if (userFolderDTO == null) {
            return null;
        }

        StorageSourceDO storageSource = null;

        if (userFolderDTO.getId() == 0) {
            storageSource = storageSourceService.getDefaultStorageSource(userId);
            if (storageSource != null) {
                userFolderDTO.setStorageSource(storageSource);
                userFolderDTO.setStorageSourceId(storageSource.getId());
            }
        } else if (userFolderDTO.getStorageSourceId() != null) {
            storageSource = storageSourceService.getStorageSourceById(userFolderDTO.getStorageSourceId(), userId);
            if (storageSource != null) {
                userFolderDTO.setStorageSource(storageSource);
            }
        }

        if (userFolderDTO.getStorageSource() == null) {
            return null;
        }

        return userFolderDTO;
    }

    /**
     * 新增UserFileTree结构对象
     *
     * @param userFileTreeList      要插入的元素集合
     * @param userId                用户ID
     * @param targetFolder          父文件夹
     * @param targetStorageSourceId 目标存储源ID
     */
    @Tran
    private void insertUserFileTree(List<UserFileTreeDTO> userFileTreeList, Long userId, UserFileDO targetFolder) {
        if (CollUtil.isEmpty(userFileTreeList)) {
            return;
        }

        var userFileList = new ArrayList<UserFileDO>();
        Long targetStorageSourceId = targetFolder.getStorageSourceId();

        for (var userFileTree : userFileTreeList) {
            var userFileDO = new UserFileDO();
            BeanUtil.copyProperties(userFileTree, userFileDO);

            userFileDO.setId(null);
            userFileDO.setUserId(userId);
            userFileDO.setParentId(targetFolder.getId());
            userFileDO.setCreateTime(null);
            userFileDO.setUpdateTime(null);

            // 复制操作：统一使用目标父目录的存储源，并设置为继承类型
            Long originalStorageSourceId = userFileDO.getStorageSourceId();
            userFileDO.setStorageSourceId(targetStorageSourceId);
            userFileDO.setStorageSourceType(1); // 设置为继承类型

            // 如果是文件，且新的存储源和原先不一致，设置迁移标记
            if (UserFileItemTypeEnum.isFile(userFileDO.getItemType())
                    && !Objects.equals(originalStorageSourceId, targetStorageSourceId)) {
                userFileDO.setMigrationPending(1);
            } else {
                userFileDO.setMigrationPending(0);
            }

            userFileList.add(userFileDO);
        }

        var saveBatchResult = this.saveBatch(userFileList);

        if (!saveBatchResult) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 递归插入子节点
        UserFileTreeDTO userFileTree;
        for (int i = 0; i < userFileTreeList.size(); i++) {
            userFileTree = userFileTreeList.get(i);
            if (UserFileItemTypeEnum.isFile(userFileTree.getItemType())
                    || CollUtil.isEmpty(userFileTree.getChildUserFileDTOList())) {
                continue;
            }

            // 插入子节点对象，传递当前节点的存储源ID
            insertUserFileTree(userFileTree.getChildUserFileDTOList(), userId, userFileList.get(i));
        }
    }

    /**
     * 获取全部要删除的对象
     *
     * @param userFileTreeList 要删除的元素集合
     * @param delIds           要删除的元素ID集合
     * @param recycleBinList   回收站元素集合
     * @param userId           用户ID
     * @param recycleId        回收ID
     * @param root             是否是根节点 1 是根节点，0 不是
     */
    private void getDelInfo(List<UserFileTreeDTO> userFileTreeList, List<Long> delIds,
            List<RecycleBinDO> recycleBinList, Long userId, String recycleId, Integer root) {
        if (CollUtil.isEmpty(userFileTreeList)) {
            return;
        }

        RecycleBinDO recycleBinDO;
        var tempRecycleId = recycleId;
        for (var userFileTree : userFileTreeList) {
            delIds.add(userFileTree.getId());

            recycleBinDO = new RecycleBinDO();
            if (StrUtil.isEmpty(recycleId)) {
                tempRecycleId = IdUtil.randomUUID();
            }
            recycleBinDO.setRecycleId(tempRecycleId);
            recycleBinDO.setRoot(root);
            recycleBinDO.setUserFileId(userFileTree.getId());
            recycleBinDO.setUserId(userId);

            recycleBinList.add(recycleBinDO);

            if (UserFileItemTypeEnum.isFile(userFileTree.getItemType())
                    || CollUtil.isEmpty(userFileTree.getChildUserFileDTOList())) {
                continue;
            }

            // 递归删除子节点对象
            getDelInfo(userFileTree.getChildUserFileDTOList(), delIds, recycleBinList, userId, tempRecycleId, 0);
        }
    }

    @Override
    public DownloadedFile download(List<UserFileTreeDTO> userFileTreeDTOList, final Long userId) throws IOException {
        return userFileDownloadService.download(userFileTreeDTOList, userId);
    }
}