package com.xiaobai1226.aether.core.cache;

import com.xiaobai1226.aether.core.config.CaffeineCacheConfig;
import com.github.benmanes.caffeine.cache.Cache;
import com.xiaobai1226.aether.core.util.CacheKeyGenerator;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.Duration;

import static com.xiaobai1226.aether.core.constant.CacheKeyConsts.CAPTCHA;
import static com.xiaobai1226.aether.core.enums.CaptchaTypeEnum.IMAGE;

/**
 * 验证码缓存DAO
 *
 * @author bai
 */
@Component
public class CaptchaCache {

    /**
     * 验证码缓存
     */
    @Inject("captchaCache")
    private Cache<String, CaffeineCacheConfig.CacheEntry<String>> captchaCache;

    /**
     * 存储图片验证码数据
     *
     * @param id          身份id
     * @param captchaCode 验证码Code
     * @param timeout     超时时间，单位 分钟
     */
    public void setImageCaptcha(String id, String captchaCode, Integer timeout) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CAPTCHA, IMAGE.type(), id);
        long expireTime = System.currentTimeMillis() + Duration.ofMinutes(timeout).toMillis();
        CaffeineCacheConfig.CacheEntry<String> entry = new CaffeineCacheConfig.CacheEntry<>(captchaCode, expireTime);
        captchaCache.put(key, entry);
    }

    /**
     * 获取并删除图片验证码数据
     *
     * @param id 身份id
     * @return id对应的图形验证码数据
     */
    public String getAndDelImageCaptcha(String id) {
        String key = CacheKeyGenerator.PROJECT.generateKey(CAPTCHA, IMAGE.type(), id);
        CaffeineCacheConfig.CacheEntry<String> entry = captchaCache.getIfPresent(key);
        if (entry != null && !entry.isExpired()) {
            captchaCache.invalidate(key);
            return entry.getValue();
        }
        // 如果过期了，也删除掉
        if (entry != null) {
            captchaCache.invalidate(key);
        }
        return null;
    }

    /**
     * 存储邮箱验证码数据
     *
     * @param event       事件
     * @param email       邮箱
     * @param captchaCode 验证码Code
     * @param timeout     超时时间，单位 分钟
     */
    // public void setEmailCaptcha(EmailEventEnum event, String email, String
    // captchaCode, Integer timeout) {
    // String key = RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.CAPTCHA,
    // CaptchaTypeEnum.EMAIL.type(), event.eventName(), email);
    // long expireTime = System.currentTimeMillis() +
    // Duration.ofMinutes(timeout).toMillis();
    // CaffeineCacheConfig.CacheEntry<String> entry = new
    // CaffeineCacheConfig.CacheEntry<>(captchaCode, expireTime);
    // captchaCache.put(key, entry);
    // }

    /**
     * 获取邮箱验证码数据
     *
     * @param event 事件
     * @param email 邮箱
     * @return id对应的图形验证码数据
     */
    // public String getEmailCaptcha(EmailEventEnum event, String email) {
    // String key = RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.CAPTCHA,
    // CaptchaTypeEnum.EMAIL.type(), event.eventName(), email);
    // CaffeineCacheConfig.CacheEntry<String> entry =
    // captchaCache.getIfPresent(key);
    // if (entry != null && !entry.isExpired()) {
    // return entry.getValue();
    // }
    // // 如果过期了，删除掉
    // if (entry != null) {
    // captchaCache.invalidate(key);
    // }
    // return null;
    // }

    /**
     * 删除邮箱验证码数据
     *
     * @param event 事件
     * @param email 邮箱
     * @return 删除结果 true 删除成功 false 删除失败
     */
    // public Boolean delEmailCaptcha(EmailEventEnum event, String email) {
    // String key = RedisKeyGenerator.PROJECT.generateKey(RedisKeyConsts.CAPTCHA,
    // CaptchaTypeEnum.EMAIL.type(), event.eventName(), email);
    // captchaCache.invalidate(key);
    // return true;
    // }
}
