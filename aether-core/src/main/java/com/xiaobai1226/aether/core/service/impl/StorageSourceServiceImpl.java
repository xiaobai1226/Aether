package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.domain.dto.StorageSourceDTO;
import com.xiaobai1226.aether.core.domain.vo.AddStorageSourceVO;
import com.xiaobai1226.aether.core.domain.vo.UpdateStorageSourceVO;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.dao.domain.entity.StorageSourceDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.StorageSourceMapper;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;
import org.noear.solon.data.tran.TranPolicy;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.*;
import static com.xiaobai1226.aether.common.enums.StorageSourceStatusEnum.ENABLED;

/**
 * 存储源服务实现类
 *
 * @author bai
 */
@Component
public class StorageSourceServiceImpl implements StorageSourceService {

    @Db
    private StorageSourceMapper storageSourceMapper;

    @Db
    private UserFileMapper userFileMapper;

    @Override
    public List<StorageSourceDTO> getStorageSourceList(Long userId) {
        var storageSourceList = ChainWrappers.lambdaQueryChain(storageSourceMapper)
                .eq(StorageSourceDO::getUserId, userId)
                .eq(StorageSourceDO::getStatus, ENABLED.getStatus())
                .orderByDesc(StorageSourceDO::getIsDefault)
                .orderByDesc(StorageSourceDO::getCreateTime)
                .list();

        return BeanUtil.copyToList(storageSourceList, StorageSourceDTO.class);
    }

    @Override
    public boolean hasStorageSource(Long userId) {
        var count = ChainWrappers.lambdaQueryChain(storageSourceMapper)
                .eq(StorageSourceDO::getUserId, userId)
                .eq(StorageSourceDO::getStatus, ENABLED.getStatus())
                .count();

        return count > 0;
    }

    @Override
    public StorageSourceDO getDefaultStorageSource(Long userId) {
        return ChainWrappers.lambdaQueryChain(storageSourceMapper)
                .eq(StorageSourceDO::getUserId, userId)
                .eq(StorageSourceDO::getIsDefault, 1)
                .eq(StorageSourceDO::getStatus, ENABLED.getStatus())
                .one();
    }

    @Override
    public StorageSourceDO getStorageSourceById(Long id, Long userId) {
        return ChainWrappers.lambdaQueryChain(storageSourceMapper)
                .eq(StorageSourceDO::getId, id)
                .eq(StorageSourceDO::getUserId, userId)
                .eq(StorageSourceDO::getStatus, ENABLED.getStatus())
                .one();
    }

    @Override
    public StorageSourceDO getStorageSourceById(Long id) {
        // 系统级查询，不校验userId，仅校验存储源状态
        return ChainWrappers.lambdaQueryChain(storageSourceMapper)
                .eq(StorageSourceDO::getId, id)
                .eq(StorageSourceDO::getStatus, ENABLED.getStatus())
                .one();
    }

    @Override
    @Tran(policy = TranPolicy.required)
    public boolean addStorageSource(AddStorageSourceVO addStorageSourceVO, Long userId) {
        // 验证路径格式（必须是绝对路径）
        if (!isAbsolutePath(addStorageSourceVO.getPath())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_STORAGE_SOURCE_PATH_FORMAT);
        }

        // 验证路径是否可用
        if (!validateStoragePath(addStorageSourceVO.getPath(), userId)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_STORAGE_SOURCE_PATH_UNAVAILABLE);
        }

        // 检查路径是否已存在
        var existingStorage = ChainWrappers.lambdaQueryChain(storageSourceMapper)
                .eq(StorageSourceDO::getUserId, userId)
                .eq(StorageSourceDO::getPath, addStorageSourceVO.getPath())
                .eq(StorageSourceDO::getStatus, ENABLED.getStatus())
                .one();

        if (existingStorage != null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_STORAGE_SOURCE_PATH_EXIST);
        }

        // 检查是否是用户的第一个存储源
        var hasStorage = hasStorageSource(userId);
        var isDefault = addStorageSourceVO.getIsDefault() != null && addStorageSourceVO.getIsDefault() == 1;

        // 如果是第一个存储源，强制设为默认
        if (!hasStorage) {
            isDefault = true;
        }

        // 如果要设为默认，需要先取消其他默认存储源
        if (isDefault) {
            ChainWrappers.lambdaUpdateChain(storageSourceMapper)
                    .eq(StorageSourceDO::getUserId, userId)
                    .eq(StorageSourceDO::getIsDefault, 1)
                    .set(StorageSourceDO::getIsDefault, 0)
                    .update();
        }

        // 创建存储源记录
        var storageSourceDO = new StorageSourceDO();
        storageSourceDO.setUserId(userId);
        storageSourceDO.setName(addStorageSourceVO.getName());
        storageSourceDO.setType(addStorageSourceVO.getType());
        storageSourceDO.setPath(addStorageSourceVO.getPath());
        storageSourceDO.setIsDefault(isDefault ? 1 : 0);
        storageSourceDO.setStatus(ENABLED.getStatus());

        var result = storageSourceMapper.insert(storageSourceDO);

        // 创建必要的子目录
        if (result > 0) {
            createSystemDirectories(addStorageSourceVO.getPath());
        }

        return result > 0;
    }

    @Override
    public boolean updateStorageSource(UpdateStorageSourceVO updateStorageSourceVO, Long userId) {
        // 检查存储源是否存在
        var storageSource = getStorageSourceById(updateStorageSourceVO.getId(), userId);
        if (storageSource == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_STORAGE_SOURCE_NO_EXIST);
        }

        // 只允许修改名称
        return ChainWrappers.lambdaUpdateChain(storageSourceMapper)
                .eq(StorageSourceDO::getId, updateStorageSourceVO.getId())
                .eq(StorageSourceDO::getUserId, userId)
                .set(StorageSourceDO::getName, updateStorageSourceVO.getName())
                .update();
    }

    @Override
    @Tran(policy = TranPolicy.required)
    public boolean deleteStorageSource(Long id, Long userId) {
        // 检查存储源是否存在
        var storageSource = getStorageSourceById(id, userId);
        if (storageSource == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_STORAGE_SOURCE_NO_EXIST);
        }

        // 不能删除默认存储源
        if (storageSource.getIsDefault() == 1) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_CANNOT_DELETE_DEFAULT_STORAGE);
        }

        // 检查是否有文件使用该存储源
        var fileCount = ChainWrappers.lambdaQueryChain(userFileMapper)
                .eq(UserFileDO::getUserId, userId)
                .eq(UserFileDO::getStorageSourceId, id)
                .count();

        if (fileCount > 0) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_CANNOT_DELETE_STORAGE_WITH_FILES);
        }

        // 删除（软删除，设置状态为禁用）
        return ChainWrappers.lambdaUpdateChain(storageSourceMapper)
                .eq(StorageSourceDO::getId, id)
                .eq(StorageSourceDO::getUserId, userId)
                .set(StorageSourceDO::getStatus, 0)
                .update();
    }

    @Override
    @Tran(policy = TranPolicy.required)
    public boolean setDefaultStorageSource(Long id, Long userId) {
        // 检查存储源是否存在
        var storageSource = getStorageSourceById(id, userId);
        if (storageSource == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_STORAGE_SOURCE_NO_EXIST);
        }

        // 如果已经是默认，直接返回成功
        if (storageSource.getIsDefault() == 1) {
            return true;
        }

        // 取消其他默认存储源
        ChainWrappers.lambdaUpdateChain(storageSourceMapper)
                .eq(StorageSourceDO::getUserId, userId)
                .eq(StorageSourceDO::getIsDefault, 1)
                .set(StorageSourceDO::getIsDefault, 0)
                .update();

        // 设置新的默认存储源
        return ChainWrappers.lambdaUpdateChain(storageSourceMapper)
                .eq(StorageSourceDO::getId, id)
                .eq(StorageSourceDO::getUserId, userId)
                .set(StorageSourceDO::getIsDefault, 1)
                .update();
    }

    @Override
    public boolean validateStoragePath(String path, Long userId) {
        try {
            Path dirPath = Paths.get(path);

            // 检查路径是否存在
            if (!Files.exists(dirPath)) {
                // 尝试创建目录
                try {
                    Files.createDirectories(dirPath);
                } catch (Exception e) {
                    return false;
                }
            }

            // 检查是否是目录
            if (!Files.isDirectory(dirPath)) {
                return false;
            }

            // 检查是否可写
            if (!Files.isWritable(dirPath)) {
                return false;
            }

            // 尝试创建测试文件
            Path testFile = dirPath.resolve(".aether_test");
            try {
                Files.write(testFile, "test".getBytes());
                Files.delete(testFile);
            } catch (Exception e) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否是绝对路径
     *
     * @param path 路径
     * @return 是否是绝对路径
     */
    private boolean isAbsolutePath(String path) {
        if (StrUtil.isEmpty(path)) {
            return false;
        }

        // Windows绝对路径：C:\ 或 \\
        // Unix/Linux绝对路径：/
        return path.matches("^([a-zA-Z]:[\\\\/]|/).*");
    }

    /**
     * 创建系统目录
     *
     * @param basePath 基础路径
     */
    private void createSystemDirectories(String basePath) {
        try {
            // 创建upload目录（用于存储用户文件）
            String uploadPath = FileUtil.normalize(basePath + File.separator + "upload");
            FileUtil.mkdir(uploadPath);
        } catch (Exception e) {
            // 目录创建失败不影响主流程
        }
    }
}