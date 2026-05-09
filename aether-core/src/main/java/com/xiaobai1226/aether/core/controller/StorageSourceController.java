package com.xiaobai1226.aether.core.controller;

import com.xiaobai1226.aether.common.domain.dto.Result;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.annotation.CurrentUserId;
import com.xiaobai1226.aether.core.domain.dto.StorageSourceDTO;
import com.xiaobai1226.aether.core.domain.vo.AddStorageSourceVO;
import com.xiaobai1226.aether.core.domain.vo.UpdateStorageSourceVO;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.NotNull;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;

import static com.xiaobai1226.aether.common.constant.GateWayTagConsts.API_V1;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;

/**
 * 存储源Controller
 *
 * @author bai
 */
@Component(tag = API_V1)
@Mapping("/storageSource")
@Valid
@Slf4j
public class StorageSourceController {

    @Inject
    private StorageSourceService storageSourceService;

    /**
     * 获取用户的存储源列表
     */
    @Get
    @Mapping("/list")
    public List<StorageSourceDTO> getStorageSourceList(@CurrentUserId Long userId) {
        return storageSourceService.getStorageSourceList(userId);
    }

    /**
     * 检查用户是否有存储源
     */
    @Get
    @Mapping("/hasStorageSource")
    public boolean hasStorageSource(@CurrentUserId Long userId) {
        return storageSourceService.hasStorageSource(userId);
    }

    /**
     * 添加存储源
     */
    @Post
    @Mapping("/add")
    public Result<Void> addStorageSource(@Validated AddStorageSourceVO addStorageSourceVO, @CurrentUserId Long userId) {
        var result = storageSourceService.addStorageSource(addStorageSourceVO, userId);

        if (!result) {
            throw new FailResultException(BAD_REQUEST_ERROR);
        }

        return Result.success("添加存储源成功");
    }

    /**
     * 更新存储源
     */
    @Post
    @Mapping("/update")
    public Result<Void> updateStorageSource(@Validated UpdateStorageSourceVO updateStorageSourceVO, @CurrentUserId Long userId) {
        var result = storageSourceService.updateStorageSource(updateStorageSourceVO, userId);

        if (!result) {
            throw new FailResultException(BAD_REQUEST_ERROR);
        }

        return Result.success("更新存储源成功");
    }

    /**
     * 删除存储源
     */
    @Post
    @Mapping("/delete")
    public Result<Void> deleteStorageSource(@Validated @NotNull(message = ERROR_STORAGE_SOURCE_ID_EMPTY) Long id, @CurrentUserId Long userId) {
        var result = storageSourceService.deleteStorageSource(id, userId);

        if (!result) {
            throw new FailResultException(BAD_REQUEST_ERROR);
        }

        return Result.success("删除存储源成功");
    }

    /**
     * 设置默认存储源
     */
    @Post
    @Mapping("/setDefault")
    public Result<Void> setDefaultStorageSource(@Validated @NotNull(message = ERROR_STORAGE_SOURCE_ID_EMPTY) Long id, @CurrentUserId Long userId) {
        var result = storageSourceService.setDefaultStorageSource(id, userId);

        if (!result) {
            throw new FailResultException(BAD_REQUEST_ERROR);
        }

        return Result.success("设置默认存储源成功");
    }
}