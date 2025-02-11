package com.xiaobai1226.aether.admin.service.intf;

import com.xiaobai1226.aether.admin.domain.vo.FileVO;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.FileDO;

import java.util.List;

/**
 * 文件服务接口
 *
 * @author bai
 */
public interface FileService {

    /**
     * 获取文件列表
     *
     * @param fileVO 文件信息
     * @return 获取到的用户数据
     * @author bai
     */
    PageResult<FileDO> getFileList(FileVO fileVO);

    /**
     * 获取没有缩略图文件列表
     */
    List<FileDO> getNoThumbnailFileDOList(List<Integer> ids);

    /**
     * 通过ID获取文件信息
     *
     * @param ids 文件ID集合
     */
//    List<FileDO> getFileDOListByIds(List<Integer> ids);

    /**
     * 生成文件缩略图
     */
    void generateThumbnails(List<FileDO> fileDOList);

    /**
     * 更新File缩略图
     */
    Boolean updateFileThumbnail(Integer id, String thumbnail);
}
