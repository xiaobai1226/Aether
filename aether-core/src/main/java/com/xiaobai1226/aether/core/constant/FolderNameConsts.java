package com.xiaobai1226.aether.core.constant;

import cn.hutool.core.util.StrUtil;

/**
 * 文件夹名称常量类
 */
public class FolderNameConsts {

    /**
     * 文件根目录
     */
    public static final String FOLDER_ROOT_FILE = "file";

    /**
     * 头像目录
     */
    public static final String PATH_AVATAR_FILE = "avatar";

    /**
     * 上传文件临时目录
     */
    public static final String PATH_TEMP_FILE = "temp";

    /**
     * 上传文件目录
     */
    public static final String PATH_UPLOAD_FILE = "upload";

    /**
     * 缩略图目录
     */
    public static final String PATH_THUMBNAIL_FILE = "thumbnail";

    /**
     * 上传文件目录全路径
     */
    public static final String PATH_UPLOAD_FILE_FULL = FOLDER_ROOT_FILE + StrUtil.SLASH + PATH_UPLOAD_FILE;

    /**
     * 上传缩略图文件目录全路径
     */
    public static final String PATH_THUMBNAIL_FILE_FULL = FOLDER_ROOT_FILE + StrUtil.SLASH + PATH_THUMBNAIL_FILE;

    /**
     * 上传文件临时目录全路径
     */
    public static final String PATH_TEMP_FILE_FULL = FOLDER_ROOT_FILE + StrUtil.SLASH + PATH_TEMP_FILE;

    /**
     * 上传头像文件目录全路径
     */
    public static final String PATH_AVATAR_FILE_FULL = FOLDER_ROOT_FILE + StrUtil.SLASH + PATH_AVATAR_FILE;
}
