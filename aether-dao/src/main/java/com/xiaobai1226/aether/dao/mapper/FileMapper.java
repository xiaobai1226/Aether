package com.xiaobai1226.aether.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件表Mapper
 *
 * @author bai
 */
public interface FileMapper extends BaseMapper<FileDO> {
    
    /**
     * 查找无引用的File记录ID列表
     * 
     * @param limit 查询数量限制
     * @return 孤立File的ID列表
     */
    List<Long> findOrphanFileIds(@Param("limit") int limit);
}
