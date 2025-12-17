package com.xiaobai1226.aether.core.service.impl;

import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.cache.UserCache;
import com.xiaobai1226.aether.core.service.intf.QuotaService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.dao.domain.entity.UserDO;
import com.xiaobai1226.aether.dao.mapper.UserMapper;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_INSUFFICIENT_STORAGE;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;

/**
 * 配额/空间服务实现（单实例、无中间件）
 *
 * 说明：当前实现以“集中入口、减少漏改”为目标，尽量复用现有 usedStorage 与 uploading 预占机制。
 */
@Component
public class QuotaServiceImpl implements QuotaService {

    @Inject
    private UserService userService;

    @Inject
    private UserCache userCache;

    @Db
    private UserMapper userMapper;

    @Override
    public void checkEnough(Long userId, long bytes) {
        if (bytes <= 0) {
            return;
        }
        var usage = userService.getUserSpaceUsage(userId);
        if (usage == null || usage.getRealRemainStorage() < bytes) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
        }
    }

    @Override
    public void reserveUploading(Long userId, long bytes) {
        if (bytes <= 0) {
            return;
        }
        checkEnough(userId, bytes);
        userCache.incrementUploadingFileSize(userId, bytes);
    }

    @Override
    public void releaseUploading(Long userId, long bytes) {
        if (bytes <= 0) {
            return;
        }
        userCache.decrementUploadingFileSize(userId, bytes);
    }

    @Override
    public void increaseUsed(Long userId, long bytes) {
        if (bytes <= 0) {
            return;
        }
        var user = userMapper.selectById(userId);
        if (user == null) {
            throw new FailResultException(SYSTEM_ERROR);
        }
        Long used = user.getUsedStorage() == null ? 0L : user.getUsedStorage();
        var update = new UserDO();
        update.setId(userId);
        update.setUsedStorage(used + bytes);
        userMapper.updateById(update);
    }

    @Override
    public void decreaseUsed(Long userId, long bytes) {
        if (bytes <= 0) {
            return;
        }
        var user = userMapper.selectById(userId);
        if (user == null) {
            throw new FailResultException(SYSTEM_ERROR);
        }
        Long used = user.getUsedStorage() == null ? 0L : user.getUsedStorage();
        long next = used - bytes;
        if (next < 0) {
            next = 0;
        }
        var update = new UserDO();
        update.setId(userId);
        update.setUsedStorage(next);
        userMapper.updateById(update);
    }
}