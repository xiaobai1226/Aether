package com.xiaobai1226.aether.core.constant;

/**
 * 系统常量类
 */
public class SystemConsts {

    /**
     * B对应的字节
     */
    public static final long B = 1;

    /**
     * KB对应的字节
     */
    public static final long KB = 1024 * B;

    /**
     * MB对应的字节
     */
    public static final long MB = 1024 * KB;

    /**
     * GB对应的字节
     */
    public static final long GB = 1024 * MB;

    /**
     * TB对应的字节
     */
    public static final long TB = 1024 * GB;

    /**
     * 默认用户初始总空间大小 单位：KB
     */
    public static final long DEFAULT_USER_TOTAL_SPACE = 5 * MB;

    /**
     * 上传文件的临时文件信息过期时间 单位：分钟
     */
    public static final int UPLOAD_TEMP_FILE_INFO_TIMEOUT = 30;

    /**
     * 上传中文件大小超时时间 单位：分钟
     */
    public static final int UPLOADING_USED_STORAGE_TIMEOUT = 120;

    /**
     * 上传文件缓冲区 1M
     */
    public static final int UPLOAD_FILE_BUFFER_SIZE_1M = (int) MB;

    /**
     * 上传文件缓冲区 32KB
     */
    public static final int UPLOAD_FILE_BUFFER_SIZE_32KB = (int) (32 * KB);

    /**
     * 下载文件的sign过期时间 单位：分钟
     */
    public static final int DOWNLOAD_FILE_SIGN_TIMEOUT = 5;

    /**
     * 头像后缀
     */
    public static final String AVATAR_SUFFIX = ".jpg";

    /**
     * 缩略图后缀
     */
    public static final String THUMBNAIL_SUFFIX = ".jpg";

    /**
     * 默认头像文件名
     */
    public static final String DEFAULT_AVATAR_FILE_NAME = "default_avatar" + AVATAR_SUFFIX;
}
