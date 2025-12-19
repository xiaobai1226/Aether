package com.xiaobai1226.aether.core.service.intf;

import com.xiaobai1226.aether.core.domain.dto.StorageSourceDTO;
import com.xiaobai1226.aether.core.domain.vo.AddStorageSourceVO;
import com.xiaobai1226.aether.core.domain.vo.UpdateStorageSourceVO;
import com.xiaobai1226.aether.dao.domain.entity.StorageSourceDO;

import java.util.List;

/**
 * 存储源服务接口
 *
 * @author bai
 */
public interface StorageSourceService {

    /**
     * 获取用户的存储源列表
     *
     * @param userId 用户ID
     * @return 存储源列表
     */
    List<StorageSourceDTO> getStorageSourceList(Long userId);

    /**
     * 检查用户是否有存储源
     *
     * @param userId 用户ID
     * @return 是否有存储源
     */
    boolean hasStorageSource(Long userId);

    /**
     * 获取用户的默认存储源
     *
     * @param userId 用户ID
     * @return 默认存储源
     */
    StorageSourceDO getDefaultStorageSource(Long userId);

    /**
     * 根据ID获取存储源
     *
     * @param id     存储源ID
     * @param userId 用户ID
     * @return 存储源
     */
    StorageSourceDO getStorageSourceById(Long id, Long userId);

    /**
     * 根据ID获取存储源（系统级，不校验用户）
     * 
     * 用于系统级操作（如定时清理任务）直接通过存储源ID获取存储源信息
     *
     * @param id 存储源ID
     * @return 存储源
     */
    StorageSourceDO getStorageSourceById(Long id);

    /**
     * 添加存储源
     *
     * @param addStorageSourceVO 添加存储源VO
     * @param userId             用户ID
     * @return 是否成功
     */
    boolean addStorageSource(AddStorageSourceVO addStorageSourceVO, Long userId);

    /**
     * 更新存储源
     *
     * @param updateStorageSourceVO 更新存储源VO
     * @param userId                用户ID
     * @return 是否成功
     */
    boolean updateStorageSource(UpdateStorageSourceVO updateStorageSourceVO, Long userId);

    /**
     * 删除存储源
     *
     * @param id     存储源ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteStorageSource(Long id, Long userId);

    /**
     * 设置默认存储源
     *
     * @param id     存储源ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean setDefaultStorageSource(Long id, Long userId);

    /**
     * 验证存储路径是否可用
     *
     * @param path   路径
     * @param userId 用户ID
     * @return 是否可用
     */
    boolean validateStoragePath(String path, Long userId);
}