package com.xiaobai1226.aether.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaobai1226.aether.core.domain.dto.UserFileTreeDTO;
import com.xiaobai1226.aether.core.domain.entity.UserFileDO;
import com.xiaobai1226.aether.core.domain.dto.UserFileDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户文件表Mapper
 *
 * @author bai
 */
public interface UserFileMapper extends BaseMapper<UserFileDO> {

    /**
     * 获取文件列表
     *
     * @param page           分页对象 为null则为不分页
     * @param userFileDO     用户文件数据属性
     * @param sortFieldIndex 排序字段索引 1 文件名 2 文件修改日期 3 文件大小
     * @param sortOrder      排序顺序，1 升序 2 降序
     * @return 文件列表
     */
    List<UserFileDTO> getFileListByPage(IPage<UserFileDTO> page, @Param("userFileDO") UserFileDO userFileDO, @Param("sortFieldIndex") Integer sortFieldIndex, @Param("sortOrder") Integer sortOrder);

    /**
     * 获取文件列表
     *
     * @param userFileDO 用户文件数据属性
     * @return 文件列表
     */
    List<UserFileDTO> getUserFileDTOByIds(@Param("userFileDO") UserFileDO userFileDO, @Param("ids") List<Integer> ids);

    /**
     * 获取文件根据parentId与名称
     *
     * @param userFileDO 用户文件数据属性
     * @return 文件列表
     */
    UserFileDTO getUserFileDTOByNameAndParentId(@Param("userFileDO") UserFileDO userFileDO);


    /**
     * 获取文件列表
     *
     * @param userFileDO 用户文件数据属性
     * @return 文件列表
     */
    List<UserFileTreeDTO> getUserFileTreeDTOByIdsAndUserId(@Param("userFileDO") UserFileDO userFileDO, @Param("ids") List<Integer> ids);

    /**
     * 获取文件列表
     *
     * @param userFileDO 用户文件数据属性
     * @return 文件列表
     */
    List<UserFileTreeDTO> getUserFileDTOByParentIdAndUserId(@Param("userFileDO") UserFileDO userFileDO);

    /**
     * 查询用户已使用空间
     * <p>
     * TODO 废弃
     *
     * @param userId 用户ID
     * @return 已使用空间
     */
    Long getUsedStorage(Integer userId);
}
