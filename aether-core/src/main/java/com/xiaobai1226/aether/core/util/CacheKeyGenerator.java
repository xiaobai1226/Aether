package com.xiaobai1226.aether.core.util;

import cn.hutool.core.util.StrUtil;

/**
 * Cache键值工具类
 *
 * @author bai
 */

public enum CacheKeyGenerator {
    // 项目名称前缀
    PROJECT("AETHER");

    /**
     * key前缀
     */
    private String prefix;

    CacheKeyGenerator(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 生成key
     *
     * @author bai
     */
    public String generateKey(Object... keys) {
        StringBuilder keyBuilder = new StringBuilder(this.prefix);
        for (Object key : keys) {
            keyBuilder.append(StrUtil.COLON).append(key);
        }
        return keyBuilder.toString();
    }

}