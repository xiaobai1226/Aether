package com.xiaobai1226.aether.core.dao.redis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.core.constant.RedisKeyConsts;
import com.xiaobai1226.aether.core.domain.dto.UploadFileTempDTO;
import com.xiaobai1226.aether.core.util.RedisKeyGenerator;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Map;

import static com.xiaobai1226.aether.core.constant.RedisKeyConsts.UPLOADED_SIZE;
import static com.xiaobai1226.aether.core.constant.SystemConsts.UPLOAD_TEMP_FILE_INFO_TIMEOUT;

/**
 * 文件Redis DAO
 *
 * @author bai
 */
@Component
public class FileRedisDAO {

    /**
     * redisClient
     */
    @Inject
    private RedissonClient redisClient;

    /**
     * 存储上传文件临时信息
     *
     * @param userId            用户id
     * @param taskId            任务id
     * @param uploadFileTempDTO 上传文件信息
     */
    public void putUploadTempFileInfo(Integer userId, String taskId, UploadFileTempDTO uploadFileTempDTO) {
        var uploadFileTempMap = BeanUtil.beanToMap(uploadFileTempDTO);

        // 将信息写入Redis
        RMap<String, Object> rMap = redisClient.getMap(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.FILE, RedisKeyConsts.UPLOAD, RedisKeyConsts.TEMP, userId, taskId));
        rMap.putAll(uploadFileTempMap);

        // 设置超时时间
        rMap.expire(Duration.ofMinutes(UPLOAD_TEMP_FILE_INFO_TIMEOUT));
    }

    /**
     * 更新已上传文件大小
     *
     * @param userId    用户id
     * @param taskId    任务id
     * @param chunkSize 上传文件信息
     */
    public void updateUploadedSize(Integer userId, String taskId, Long chunkSize) {
        // 获取redisson的map对象
        RMap<String, Object> rMap = redisClient.getMap(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.FILE, RedisKeyConsts.UPLOAD, RedisKeyConsts.TEMP, userId, taskId));

        var uploadedSize = 0L;
        var uploadedSizeObject = rMap.get(UPLOADED_SIZE);
        if (uploadedSizeObject != null) {
            uploadedSize = Long.parseLong(String.valueOf(uploadedSizeObject)) + chunkSize;
        } else {
            uploadedSize = chunkSize;
        }

        // 将新值写入Redis
        rMap.put(UPLOADED_SIZE, uploadedSize);

        // 设置超时时间
        rMap.expire(Duration.ofMinutes(UPLOAD_TEMP_FILE_INFO_TIMEOUT));
    }

    /**
     * 获取上传文件临时信息
     *
     * @param userId 用户id
     * @param taskId 任务id
     * @return id对应的图形验证码数据
     */
    public UploadFileTempDTO getUploadTempFileInfo(Integer userId, String taskId) {
        // 获取redisson的map对象
        RMap<String, Object> rMap = redisClient.getMap(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.FILE, RedisKeyConsts.UPLOAD, RedisKeyConsts.TEMP, userId, taskId));

        // 获取map中的所有键值对
        Map<String, Object> uploadFileTempMap = rMap.readAllMap();
        if (CollUtil.isNotEmpty(uploadFileTempMap)) {
            return BeanUtil.toBean(uploadFileTempMap, UploadFileTempDTO.class);
        }
        return null;
    }

    /**
     * 删除上传文件临时信息
     *
     * @param userId 用户id
     * @param taskId 任务id
     */
    public Boolean delUploadTempFileInfo(Integer userId, String taskId) {
        // 获取redisson的map对象
        RMap<String, Object> rMap = redisClient.getMap(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.FILE, RedisKeyConsts.UPLOAD, RedisKeyConsts.TEMP, userId, taskId));
        // 删除键值对
        return rMap.delete();
    }
}
