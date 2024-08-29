package com.xiaobai1226.aether.core.dao.redis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.core.constant.RedisKeyConsts;
import com.xiaobai1226.aether.core.domain.dto.ShareFileDTO;
import com.xiaobai1226.aether.core.util.RedisKeyGenerator;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Map;

import static com.xiaobai1226.aether.core.constant.SystemConsts.UPLOAD_TEMP_FILE_INFO_TIMEOUT;

/**
 * 分享Redis DAO
 *
 * @author bai
 */
@Component
public class ShareRedisDAO {

    /**
     * redisClient
     */
    @Inject
    private RedissonClient redisClient;

    /**
     * 存储分享文件信息
     *
     * @param shareId      分享id
     * @param shareFileDTO 分享文件信息
     */
    public void putShareInfo(String shareId, ShareFileDTO shareFileDTO) {
        var shareFileDTOMap = BeanUtil.beanToMap(shareFileDTO);

        // 将信息写入Redis
        RMap<String, Object> rMap = redisClient.getMap(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.FILE, RedisKeyConsts.SHARE, shareId));
        rMap.putAll(shareFileDTOMap);

        // 设置超时时间 TODO 超时时间待确认
        rMap.expire(Duration.ofMinutes(UPLOAD_TEMP_FILE_INFO_TIMEOUT));
    }

    /**
     * 获取分享文件信息
     *
     * @param shareId 分享id
     * @return 分享文件信息
     */
    public ShareFileDTO getShareInfo(String shareId) {
        // 获取redisson的map对象
        RMap<String, Object> rMap = redisClient.getMap(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.FILE, RedisKeyConsts.SHARE, shareId));

        // 获取map中的所有键值对
        Map<String, Object> shareFileDTOMap = rMap.readAllMap();
        if (CollUtil.isNotEmpty(shareFileDTOMap)) {
            return BeanUtil.toBean(shareFileDTOMap, ShareFileDTO.class);
        }
        return null;
    }
}
