package com.xiaobai1226.aether.core.enums;

/**
 * 存储源枚举
 *
 * @author bai
 */
public enum StorageSourceEnum {
    /**
     * 本地
     */
    LOCAL(0, "本地");

    /**
     * 事件value成员变量
     */
    private final Integer id;

    /**
     * 事件name成员变量
     */
    private final String name;

    /**
     * 构造方法
     *
     * @param id   id
     * @param name name
     */
    StorageSourceEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 获取id
     *
     * @return id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * 获取name
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }
}
