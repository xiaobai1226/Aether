package com.xiaobai1226.aether.core.service.intf;

import com.baomidou.mybatisplus.solon.service.IService;
import com.xiaobai1226.aether.core.domain.dto.PageResultDataDTO;
import com.xiaobai1226.aether.core.domain.dto.RecycleBinFileDTO;
import com.xiaobai1226.aether.core.domain.entity.RecycleBinDO;
import com.xiaobai1226.aether.core.domain.vo.common.PageVO;

import java.util.List;

/**
 * 回收站服务接口
 *
 * @author bai
 */
public interface RecycleBinService extends IService<RecycleBinDO> {

    /**
     * 批量新增记录到数据库
     *
     * @param recycleBinList 回收站数据集合
     * @return 插入结果
     */
    Boolean insertBatch(List<RecycleBinDO> recycleBinList);

    /**
     * 获取回收站文件及文件夹列表
     *
     * @param userId       用户ID
     * @param recycleBinVO 回收站文件信息
     * @return 获取到的用户文件数据
     * @author bai
     */
    PageResultDataDTO<RecycleBinFileDTO> getRecycleBinList(Integer userId, PageVO recycleBinVO);

    /**
     * 清空回收站
     *
     * @param userId 用户ID
     * @author bai
     */
    void delete(Integer userId, List<String> recycleIds);

    /**
     * 还原回收站
     *
     * @param userId 用户ID
     * @author bai
     */
    void restore(Integer userId, List<String> recycleIds);
}
