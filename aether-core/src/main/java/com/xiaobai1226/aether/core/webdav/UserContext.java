package com.xiaobai1226.aether.core.webdav;

/**
 * WebDAV用户上下文
 * 用于在ThreadLocal中存储当前用户ID
 *
 * @author 高压锅里的小白
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除当前用户ID
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}