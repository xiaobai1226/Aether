package com.xiaobai1226.aether.core.service.support;

import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;

import java.util.List;

import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 用户文件树服务（从 UserFileServiceImpl 中抽离）
 */
@Component
public class UserFileTreeService {

    @Db
    private UserFileMapper userFileMapper;

    /**
     * 递归填充子树（文件夹才会继续向下）
     */
    public void fillSubTree(Long userId, List<UserFileTreeDTO> roots) {
        if (CollUtil.isEmpty(roots)) {
            return;
        }

        for (var node : roots) {
            if (UserFileItemTypeEnum.isFile(node.getItemType())) {
                continue;
            }

            var query = new UserFileDO();
            query.setUserId(userId);
            query.setFileStatus(NORMAL.flag());
            query.setParentId(node.getId());

            var children = userFileMapper.getUserFileDTOByParentIdAndUserId(query);
            if (CollUtil.isEmpty(children)) {
                continue;
            }

            node.setChildUserFileDTOList(children);
            fillSubTree(userId, children);
        }
    }

    /**
     * 计算树占用空间（只统计文件节点）
     */
    public long calcSpaceUsage(List<UserFileTreeDTO> roots) {
        if (CollUtil.isEmpty(roots)) {
            return 0L;
        }

        long total = 0L;
        for (var node : roots) {
            if (UserFileItemTypeEnum.isFile(node.getItemType())) {
                total += node.getSize();
                continue;
            }
            var children = node.getChildUserFileDTOList();
            if (CollUtil.isNotEmpty(children)) {
                total += calcSpaceUsage(children);
            }
        }
        return total;
    }
}