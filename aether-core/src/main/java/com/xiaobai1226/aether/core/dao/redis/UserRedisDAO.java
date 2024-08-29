package com.xiaobai1226.aether.core.dao.redis;

import com.xiaobai1226.aether.core.constant.RedisKeyConsts;
import com.xiaobai1226.aether.core.util.RedisKeyGenerator;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

import java.time.Duration;

import static com.xiaobai1226.aether.core.constant.SystemConsts.UPLOADING_USED_STORAGE_TIMEOUT;

/**
 * 用户Redis DAO
 *
 * @author bai
 */
@Component
public class UserRedisDAO {

    /**
     * redisClient
     */
    @Inject
    private RedissonClient redisClient;

    /**
     * 增加上传中文件大小
     *
     * @param userId   用户id
     * @param fileSize 文件大小
     */
    public void incrementUploadingFileSize(Integer userId, Long fileSize) {
        RAtomicLong rAtomicLongAdd = redisClient.getAtomicLong(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.USER, RedisKeyConsts.UPLOADING, RedisKeyConsts.USED_STORAGE, userId));
        // 对值进行加法操作
        rAtomicLongAdd.addAndGet(fileSize);
        // 设置超时时间
        rAtomicLongAdd.expire(Duration.ofMinutes(UPLOADING_USED_STORAGE_TIMEOUT));
    }

    /**
     * 减少上传中文件大小
     *
     * @param userId   用户id
     * @param fileSize 文件大小
     */
    public void decrementUploadingFileSize(Integer userId, Long fileSize) {
        RAtomicLong rAtomicLongDecrement = redisClient.getAtomicLong(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.USER, RedisKeyConsts.UPLOADING, RedisKeyConsts.USED_STORAGE, userId));
        // 对值进行减法操作
        var newValue = rAtomicLongDecrement.addAndGet(-fileSize);

        if (newValue < 0) {
            rAtomicLongDecrement.set(0);
        }
    }

    /**
     * 获取上传中文件大小
     *
     * @param userId 用户id
     * @return 上传中文件大小
     */
    public Long getUploadingFileSize(Integer userId) {
        // 从Redis中获取信息
        RAtomicLong rAtomicLong = redisClient.getAtomicLong(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.USER, RedisKeyConsts.UPLOADING, RedisKeyConsts.USED_STORAGE, userId));

        return rAtomicLong.get();
    }
}
