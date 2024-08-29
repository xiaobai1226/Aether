package com.xiaobai1226.aether.core.dao.redis;

import com.xiaobai1226.aether.core.enums.CaptchaTypeEnum;
import com.xiaobai1226.aether.core.enums.EmailEventEnum;
import com.xiaobai1226.aether.core.constant.RedisKeyConsts;
import com.xiaobai1226.aether.core.util.RedisKeyGenerator;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * 验证码Redis DAO
 *
 * @author bai
 */
@Component
public class CaptchaRedisDAO {

    /**
     * redisClient
     */
    @Inject
    private RedissonClient redisClient;

    /**
     * 存储图片验证码数据
     *
     * @param id          身份id
     * @param captchaCode 验证码Code
     * @param timeout     超时时间，单位 分钟
     */
    public void setImageCaptcha(String id, String captchaCode, Integer timeout) {
        // 设置键值对和超时时间
        RBucket<String> bucket = redisClient.getBucket(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.CAPTCHA, CaptchaTypeEnum.IMAGE.type(), id));
        bucket.set(captchaCode, Duration.ofMinutes(timeout));
    }

    /**
     * 获取并删除图片验证码数据
     *
     * @param id 身份id
     * @return id对应的图形验证码数据
     */
    public String getAndDelImageCaptcha(String id) {
        RBucket<String> bucket = redisClient.getBucket(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.CAPTCHA, CaptchaTypeEnum.IMAGE.type(), id));
        // 获取并删除键值对
        return bucket.getAndDelete();
    }

    /**
     * 存储邮箱验证码数据
     *
     * @param event       事件
     * @param email       邮箱
     * @param captchaCode 验证码Code
     * @param timeout     超时时间，单位 分钟
     */
    public void setEmailCaptcha(EmailEventEnum event, String email, String captchaCode, Integer timeout) {
//        redisTemplate.opsForValue().set(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.CAPTCHA, CaptchaTypeEnum.EMAIL.type(), event.eventName(), email), captchaCode, timeout, TimeUnit.MINUTES);
    }

    /**
     * 获取邮箱验证码数据
     *
     * @param event 事件
     * @param email 邮箱
     * @return id对应的图形验证码数据
     */
    public String getEmailCaptcha(EmailEventEnum event, String email) {
//        return redisTemplate.opsForValue().getAndDelete(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.CAPTCHA, CaptchaTypeEnum.EMAIL.type(), event.eventName(), email));
        return null;
    }

    /**
     * 删除邮箱验证码数据
     *
     * @param event 事件
     * @param email 邮箱
     * @return 删除结果 true 删除成功 false 删除失败
     */
    public Boolean delEmailCaptcha(EmailEventEnum event, String email) {
        return null;
//        return redisTemplate.delete(RedisKeyGenerator.PROJECT.generateKey(RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.CAPTCHA, CaptchaTypeEnum.EMAIL.type(), event.eventName(), email)));
    }
}
