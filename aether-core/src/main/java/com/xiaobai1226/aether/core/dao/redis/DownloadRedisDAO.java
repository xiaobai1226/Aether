package com.xiaobai1226.aether.core.dao.redis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.core.constant.RedisKeyConsts;
import com.xiaobai1226.aether.core.domain.dto.DownloadFileDTO;
import com.xiaobai1226.aether.core.util.RedisKeyGenerator;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Map;

import static com.xiaobai1226.aether.core.constant.SystemConsts.DOWNLOAD_FILE_SIGN_TIMEOUT;

/**
 * 下载Redis DAO
 *
 * @author bai
 */
@Component
public class DownloadRedisDAO {

    /**
     * redisClient
     */
    @Inject
    private RedissonClient redisClient;

    /**
     * 存储下载Sign数据
     *
     * @param downloadFileDTO 下载文件信息
     * @param sign            下载sign
     */
    public void setDownloadSign(DownloadFileDTO downloadFileDTO, String sign) {
        var downloadFileDTOMap = BeanUtil.beanToMap(downloadFileDTO);

        // 将信息写入Redis
        RMap<String, Object> rMap = redisClient.getMap(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.FILE, RedisKeyConsts.DOWNLOAD, sign));
        rMap.putAll(downloadFileDTOMap);

        // 设置超时时间
        rMap.expire(Duration.ofMinutes(DOWNLOAD_FILE_SIGN_TIMEOUT));
    }

    /**
     * 获取下载sign数据
     *
     * @param sign 下载sign
     * @return 下载sign对应的文件ID
     */
    public DownloadFileDTO getDownloadInfo(String sign) {
        // 获取redisson的map对象
        RMap<String, Object> rMap = redisClient.getMap(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.FILE, RedisKeyConsts.DOWNLOAD, sign));

        // 获取map中的所有键值对
        Map<String, Object> downloadFileDTOMap = rMap.readAllMap();
        if (CollUtil.isNotEmpty(downloadFileDTOMap)) {
            return BeanUtil.toBean(downloadFileDTOMap, DownloadFileDTO.class);
        }
        return null;
    }
}
