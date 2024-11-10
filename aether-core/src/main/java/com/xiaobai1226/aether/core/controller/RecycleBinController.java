package com.xiaobai1226.aether.core.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.core.domain.dto.PageResultDataDTO;
import com.xiaobai1226.aether.core.domain.dto.RecycleBinFileDTO;
import com.xiaobai1226.aether.core.domain.vo.DeleteRecycleBinVO;
import com.xiaobai1226.aether.core.domain.vo.RestoreRecycleBinVO;
import com.xiaobai1226.aether.core.domain.vo.common.PageVO;
import com.xiaobai1226.aether.core.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
import com.xiaobai1226.aether.core.util.Result;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;

import static com.xiaobai1226.aether.core.constant.GateWayTagConsts.API_V1;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_DEL_CONTENT_EMPTY;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_RESTORE_CONTENT_EMPTY;
import static com.xiaobai1226.aether.core.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.ResultSuccessMsgEnum.*;

/**
 * 回收站Controller
 *
 * @author bai
 */
@Component(tag = API_V1)
@Mapping("/recyclebin")
@Valid
public class RecycleBinController {

    @Inject
    private RecycleBinService recycleBinService;

    /**
     * 分页获取回收站列表
     *
     * @param recycleBinVO 回收站用户文件VO
     */
    @Get
    @Mapping("/getRecycleBinListByPage")
    public PageResultDataDTO<RecycleBinFileDTO> getRecycleBinListByPage(PageVO recycleBinVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        if (recycleBinVO.getSortField() == null) {
            recycleBinVO.setSortField(1);
        }
        if (recycleBinVO.getSortOrder() == null) {
            recycleBinVO.setSortOrder(1);
        }

        return recycleBinService.getRecycleBinList(userId, recycleBinVO);
    }

    /**
     * 删除回收站文件
     */
    @Post
    @Mapping("/delete")
    public Result delete(@Validated DeleteRecycleBinVO deleteRecycleBinVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        var recycleIds = Arrays.stream(deleteRecycleBinVO.getRecycleIds().split(StrUtil.COMMA)).filter(s -> !s.isEmpty()).toList();

        if (CollUtil.isEmpty(recycleIds)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DEL_CONTENT_EMPTY);
        }

        recycleBinService.delete(userId, recycleIds);

        return Result.success(SUCCESS_MSG_RECYCLE_BIN_DELETE.msg());
    }

    /**
     * 还原回收站文件
     */
    @Post
    @Mapping("/restore")
    public Result restore(@Validated RestoreRecycleBinVO restoreRecycleBinVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        List<String> recycleIds = Arrays.stream(restoreRecycleBinVO.getRecycleIds().split(StrUtil.COMMA)).filter(s -> !s.isEmpty()).toList();

        if (recycleIds.isEmpty()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_RESTORE_CONTENT_EMPTY);
        }

        recycleBinService.restore(userId, recycleIds);

        return Result.success(SUCCESS_MSG_RECYCLE_BIN_RESTORE.msg());
    }
}
