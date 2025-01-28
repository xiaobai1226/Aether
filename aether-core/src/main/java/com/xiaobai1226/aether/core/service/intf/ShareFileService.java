package com.xiaobai1226.aether.core.service.intf;

import com.baomidou.mybatisplus.solon.plugins.pagination.Page;
import com.baomidou.mybatisplus.solon.service.IService;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.core.domain.dto.ShareFileDTO;
import com.xiaobai1226.aether.core.domain.dto.ShareInfoDTO;
import com.xiaobai1226.aether.domain.entity.ShareDO;
import com.xiaobai1226.aether.domain.entity.ShareUserFileDO;
import com.xiaobai1226.aether.core.domain.vo.common.PageVO;

import java.util.List;

/**
 * 分享文件服务接口
 *
 * @author bai
 */
public interface ShareFileService extends IService<ShareUserFileDO> {

    /**
     * 创建分享文件
     *
     * @param userFileIds    用户文件ID集合
     * @param extractionCode 提取码，4位字符串，只包含字母数字
     * @param validityPeriod 有效期，单位 天，0为永久
     * @param userId         用户ID
     * @return 分享ID
     * @author bai
     */
    String create(List<Integer> userFileIds, String extractionCode, Integer validityPeriod, Integer userId);

    /**
     * 分页获取分享文件及文件夹列表
     *
     * @param userId      用户ID
     * @param shareFileVO 分享文件信息,为null代表不分页
     * @return 获取到的分享文件数据
     * @author bai
     */
    Page<ShareDO> getShareDOListByPage(Integer userId, PageVO shareFileVO);

    /**
     * 获取分享文件及文件夹列表
     *
     * @param userId   用户ID
     * @param shareIds 分享ID集合
     * @return 获取到的分享文件数据
     * @author bai
     */
    List<ShareDO> getShareDOList(Integer userId, List<String> shareIds);

    /**
     * 获取分享文件及文件夹列表
     *
     * @param userId      用户ID
     * @param shareFileVO 分享文件信息
     * @return 获取到的分享文件数据
     * @author bai
     */
    PageResult<ShareFileDTO> getShareFileList(Integer userId, PageVO shareFileVO);

    /**
     * 取消分享文件
     *
     * @param userId   用户ID
     * @param shareIds 分享ID集合
     * @author bai
     */
    void cancelShareFile(Integer userId, List<String> shareIds);

    /**
     * 获取分享信息
     *
     * @param shareId 分享ID
     * @return 分享信息
     * @author bai
     */
    ShareInfoDTO getShareInfo(String shareId);

    /**
     * 获取分享文件详情列表
     *
     * @param getShareFileInfoListVO 分享文件信息
     * @return 获取到的分享文件数据
     * @author bai
     */
//    PageResultDataDTO<UserFileDTO> getShareFileInfoListByShareId(GetShareFileInfoListVO getShareFileInfoListVO);
//
//    /**
//     * 获取分享文件及文件夹列表通过分享ID
//     *
//     * @param userId   用户ID
//     * @param shareIds 分享ID集合
//     * @return 获取到的分享文件数据
//     * @author bai
//     */
//    List<ShareDO> getShareFileDOListByShareId(Integer userId, List<String> shareIds);

//
//    /**
//     * 校验提取码
//     *
//     * @param checkExtractionCodeVO 检查校验码VO
//     * @author bai
//     */
//    void checkExtractionCode(CheckExtractionCodeVO checkExtractionCodeVO);
//
//    /**
//     * 获取分享信息
//     *
//     * @param shareId 分享ID
//     * @return 分享信息
//     * @author bai
//     */
//    ShareFileDTO getShareFileDTOByShareIdAndName(String shareId, String name);
}
