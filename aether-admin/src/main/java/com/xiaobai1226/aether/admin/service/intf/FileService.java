package com.xiaobai1226.aether.admin.service.intf;

import com.xiaobai1226.aether.admin.domain.vo.FileVO;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.FileDO;

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
}
