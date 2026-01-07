package com.xiaobai1226.aether.core.domain.dto;

import com.xiaobai1226.aether.dao.domain.entity.StorageSourceDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户文件夹DTO
 * 继承UserFileDO，并扩展存储源信息
 * 
 * 用于@FolderByPath注解自动注入文件夹信息和存储源信息
 * 适用于获取父文件夹、目标文件夹等场景
 *
 * @author bai
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class UserFolderDTO extends UserFileDO {

    /**
     * 存储源信息
     * 如果@FolderByPath注解的fetchStorageSource=false，则此字段为null
     * 如果文件夹为null（根目录），则此字段为默认存储源
     */
    private StorageSourceDO storageSource;

    /**
     * 创建根目录的UserFolderDTO（id为0）
     * 
     * @param userId 用户ID
     * @return UserFolderDTO
     */
    public static UserFolderDTO createRoot(Long userId) {
        UserFolderDTO dto = new UserFolderDTO();
        dto.setId(0L);
        dto.setParentId(null);
        dto.setUserId(userId);
        dto.setItemType(0); // 文件夹类型
        return dto;
    }
}