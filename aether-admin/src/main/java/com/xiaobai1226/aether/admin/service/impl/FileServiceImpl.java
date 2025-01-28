package com.xiaobai1226.aether.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.solon.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.solon.plugins.pagination.Page;
import com.xiaobai1226.aether.admin.domain.vo.FileVO;
import com.xiaobai1226.aether.admin.service.intf.FileService;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.FileDO;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;

/**
 * 文件service实现类
 *
 * @author bai
 */
@Component
public class FileServiceImpl implements FileService {

    @Db
    private FileMapper fileMapper;

    @Override
    public PageResult<FileDO> getFileList(FileVO fileVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(fileMapper);
        var fileListPage = lambdaQuery.page(new Page<>(fileVO.getPageNum(), fileVO.getPageSize()));

        // 判断结果是否为空
        if (fileListPage == null || CollUtil.isEmpty(fileListPage.getRecords())) {
            return null;
        }

        return PageResult.with(fileListPage);
    }
}
