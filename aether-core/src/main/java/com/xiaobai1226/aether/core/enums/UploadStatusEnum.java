package com.xiaobai1226.aether.core.enums;

/**
 * 上传状态枚举
 *
 * @author bai
 */
public enum UploadStatusEnum {
    /**
     * 秒传
     */
    UPLOAD_SECOND(0),

    /**
     * 上传中
     */
    UPLOADING(1),

    /**
     * 上传完成
     */
    UPLOAD_FINISH(2),

    /**
     * 上传失败
     */
    UPLOAD_FAIL(3);

    /**
     * id成员变量
     */
    private final Integer id;

    /**
     * 构造方法
     *
     * @param value id
     */
    UploadStatusEnum(Integer value) {
        this.id = value;
    }

    /**
     * 获取id
     *
     * @return id
     */
    public Integer id() {
        return this.id;
    }
}
