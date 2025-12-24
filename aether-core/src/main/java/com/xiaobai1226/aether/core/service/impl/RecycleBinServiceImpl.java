package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.xiaobai1226.aether.core.domain.dto.RecycleBinFileDTO;
import com.xiaobai1226.aether.core.domain.vo.common.PageVO;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
import com.xiaobai1226.aether.core.usecase.recycle.RestoreFileUseCase;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.dao.domain.entity.RecycleBinDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.RecycleBinMapper;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.*;

import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.DEL;

/**
 * 回收站服务接口实现类
 *
 * @author bai
 */
@Component
@Slf4j
public class RecycleBinServiceImpl extends ServiceImpl<RecycleBinMapper, RecycleBinDO> implements RecycleBinService {

    @Db
    private UserFileMapper userFileMapper;

    @Db
    private RecycleBinMapper recycleBinMapper;

    @Inject("${project.recycle-bin.retention-days:10}")
    private Integer retentionDays;

    @Inject
    private com.xiaobai1226.aether.core.usecase.recycle.PurgeRecycleUseCase purgeRecycleUseCase;

    @Inject
    private RestoreFileUseCase restoreFileUseCase;

    @Tran
    @Override
    public Boolean insertBatch(List<RecycleBinDO> recycleBinList) {
        if (CollUtil.isEmpty(recycleBinList)) {
            return false;
        }
        return this.saveBatch(recycleBinList);
    }

    @Override
    public PageResult<RecycleBinFileDTO> getRecycleBinList(final Long userId, PageVO recycleBinVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper).eq(RecycleBinDO::getUserId, userId)
                .eq(RecycleBinDO::getRoot, 1);

        // 1 文件名 2 删除时间 3 文件大小 4 有效时间
        // if (recycleBinVO.getSortField() == 1 && recycleBinVO.getSortOrder() == 1) {
        // lambdaQuery.orderByDesc(RecycleBinDO::get);
        // } else if (recycleBinVO.getSortField() == 1 && recycleBinVO.getSortOrder() ==
        // 2) {
        // lambdaQuery.orderByDesc(RecycleBinDO::get);
        // } else

        if (recycleBinVO.getSortField() == 2 && recycleBinVO.getSortOrder() == 1) {
            lambdaQuery.orderByAsc(RecycleBinDO::getCreateTime);
        } else if (recycleBinVO.getSortField() == 2 && recycleBinVO.getSortOrder() == 2) {
            lambdaQuery.orderByDesc(RecycleBinDO::getCreateTime);
        }
        // else if (recycleBinVO.getSortField() == 3 && recycleBinVO.getSortOrder() ==
        // 1) {
        // lambdaQuery.orderByDesc(RecycleBinDO::getCreateTime);
        // } else if (recycleBinVO.getSortField() == 3 && recycleBinVO.getSortOrder() ==
        // 2) {
        // lambdaQuery.orderByDesc(RecycleBinDO::getCreateTime);
        // } else if (recycleBinVO.getSortField() == 4 && recycleBinVO.getSortOrder() ==
        // 1) {
        // lambdaQuery.orderByDesc(RecycleBinDO::getCreateTime);
        // } else if (recycleBinVO.getSortField() == 4 && recycleBinVO.getSortOrder() ==
        // 2) {
        // lambdaQuery.orderByDesc(RecycleBinDO::get);
        // }
        var recycleBinListPage = lambdaQuery.page(new Page<>(recycleBinVO.getPageNum(), recycleBinVO.getPageSize()));

        // 判断结果是否为空
        if (recycleBinListPage == null || CollUtil.isEmpty(recycleBinListPage.getRecords())) {
            return null;
        }

        var userFileIds = new ArrayList<Long>();
        for (var recycleBin : recycleBinListPage.getRecords()) {
            userFileIds.add(recycleBin.getUserFileId());
        }

        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(DEL.flag());
        var userFileList = userFileMapper.getUserFileDTOByIds(userFileDO, userFileIds);

        if (CollUtil.isEmpty(userFileList) || recycleBinListPage.getRecords().size() != userFileList.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var recycleBinDTOList = new ArrayList<RecycleBinFileDTO>();
        for (var recycleBinDO : recycleBinListPage.getRecords()) {
            for (UserFileDTO userFileDTO : userFileList) {
                if (userFileDTO.getId().equals(recycleBinDO.getUserFileId())) {
                    var recycleBinDTO = BeanUtil.toBean(userFileDTO, RecycleBinFileDTO.class);
                    recycleBinDTO.setRecycleId(recycleBinDO.getRecycleId());
                    recycleBinDTO.setDeleteTime(recycleBinDO.getCreateTime());

                    recycleBinDTOList.add(recycleBinDTO);

                    break;
                }
            }
        }

        var recycleBinFileIPage = new Page<RecycleBinFileDTO>(recycleBinListPage.getCurrent(),
                recycleBinListPage.getSize(), recycleBinListPage.getTotal());
        recycleBinFileIPage.setPages(recycleBinListPage.getPages());
        recycleBinFileIPage.setRecords(recycleBinDTOList);
        return new PageResult<>(recycleBinFileIPage);
    }

    @Tran
    @Override
    public void delete(final Long userId, List<String> recycleIds) {
        // 委托给 UseCase 处理完整的业务逻辑（包括配额释放和物理文件删除）
        purgeRecycleUseCase.execute(recycleIds, userId);
    }

    @Tran
    @Override
    public void restore(final Long userId, List<String> recycleIds) {
        // 委托给 UseCase 处理完整的业务逻辑（包括重名检查、父目录验证和存储源迁移）
        restoreFileUseCase.execute(recycleIds, userId);
    }

    @Override
    @Tran
    public void cleanExpiredRecycleBinFiles() {
        try {
            // 计算N天前的时间（从配置中读取保留天数）
            Date expireDate = DateUtil.offsetDay(new Date(), -retentionDays);
            String expireDateStr = DateUtil.format(expireDate, "yyyy-MM-dd HH:mm:ss");

            log.info("开始清理{}之前的回收站文件（保留期限{}天）", expireDateStr, retentionDays);

            // 查询10天前的回收站记录
            var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper);
            var expiredRecycleBinList = lambdaQuery.lt(RecycleBinDO::getCreateTime, expireDateStr).list();

            if (CollUtil.isEmpty(expiredRecycleBinList)) {
                log.info("没有需要清理的过期回收站文件");
                return;
            }

            log.info("找到{}条过期回收站记录", expiredRecycleBinList.size());

            // 按用户分组处理
            Map<Long, List<String>> userRecycleIdMap = new HashMap<>();
            for (var recycleBinDO : expiredRecycleBinList) {
                userRecycleIdMap.computeIfAbsent(recycleBinDO.getUserId(), k -> new ArrayList<>())
                        .add(recycleBinDO.getRecycleId());
            }

            // 逐个用户清理
            int totalCleaned = 0;
            for (Map.Entry<Long, List<String>> entry : userRecycleIdMap.entrySet()) {
                Long userId = entry.getKey();
                List<String> recycleIds = entry.getValue();

                try {
                    // 调用删除方法
                    delete(userId, recycleIds);
                    totalCleaned += recycleIds.size();
                    log.info("用户{}的{}条过期回收站文件已清理", userId, recycleIds.size());
                } catch (Exception e) {
                    log.error("清理用户{}的过期回收站文件失败: {}", userId, e.getMessage(), e);
                }
            }

            log.info("回收站自动清理完成，共清理{}条记录", totalCleaned);
        } catch (Exception e) {
            log.error("清理过期回收站文件失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}