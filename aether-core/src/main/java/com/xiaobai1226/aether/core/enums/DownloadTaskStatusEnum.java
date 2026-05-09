package com.xiaobai1226.aether.core.enums;

/**
 * 下载任务状态枚举
 */
public enum DownloadTaskStatusEnum {
    /**
     * 排队中
     */
    PENDING(0),

    /**
     * 执行中
     */
    RUNNING(1),

    /**
     * 执行成功
     */
    SUCCEEDED(2),

    /**
     * 执行失败
     */
    FAILED(3),

    /**
     * 已过期
     */
    EXPIRED(4);

    private final Integer flag;

    DownloadTaskStatusEnum(Integer flag) {
        this.flag = flag;
    }

    public Integer flag() {
        return this.flag;
    }
}