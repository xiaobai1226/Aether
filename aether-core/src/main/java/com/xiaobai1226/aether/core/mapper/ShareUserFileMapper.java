package com.xiaobai1226.aether.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaobai1226.aether.core.domain.dto.ShareFileDTO;
import com.xiaobai1226.aether.core.domain.entity.ShareUserFileDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分享文件中间表Mapper
 *
 * @author bai
 */
public interface ShareUserFileMapper extends BaseMapper<ShareUserFileDO> {

    /**
     * 获取分享文件列表
     *
     * @param page   分页对象 为null则为不分页
     * @param userId 用户ID
     * @return 文件列表
     */
    IPage<String> getShareIdListByPage(IPage<String> page, @Param("userId") Integer userId);

    /**
     * 获取分享文件列表
     *
     * @param shareIds shareId集合
     * @param userId   用户ID
     * @return 文件列表
     */
    List<ShareFileDTO> getShareFileDTOListByShareId(@Param("userId") Integer userId, @Param("shareIds") List<String> shareIds);

    /**
     * 分页获取分享文件列表
     *
     * @param shareIds shareId集合
     * @param page     分页对象 为null则为不分页
     * @return 文件列表
     */
    IPage<ShareFileDTO> getShareFileDTOListByShareIdAndPage(IPage<String> page, @Param("userId") Integer userId, @Param("shareIds") List<String> shareIds);
}
