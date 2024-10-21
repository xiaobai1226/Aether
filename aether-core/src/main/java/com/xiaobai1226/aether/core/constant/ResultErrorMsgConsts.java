package com.xiaobai1226.aether.core.constant;

import static com.xiaobai1226.aether.core.constant.VerifyConsts.*;

/**
 * 返回错误描述常量类
 *
 * @author bai
 */
public class ResultErrorMsgConsts {

    ///////////////////////////////////////////////// 密码 ///////////////////////////////////////////////
    /**
     * 密码为空错误
     */
    public static final String ERROR_PASSWARD_EMPTY = "密码不能为空";

    /**
     * 密码格式错误
     */
    public static final String ERROR_PASSWARD_FORMAT = "密码格式不正确，只允许由" + PASSWORD_MIN_LENGTH + "到" + PASSWORD_MAX_LENGTH + "位的组合构成，必须包含至少一个小写字母，一个大写字母，一个数字，以及一个特殊字符,特殊字符包括!@#$%^&*()<>?";

    ///////////////////////////////////////////////// 邮箱验证码 ///////////////////////////////////////////////
    /**
     * 邮箱为空错误
     */
    public static final String ERROR_EMAIL_EMPTY = "邮箱不能为空";

    /**
     * 邮箱格式错误
     */
    public static final String ERROR_EMAIL_FORMAT = "邮箱格式不正确";

    /**
     * 邮箱长度错误
     */
    public static final String ERROR_EMAIL_LENGTH = "邮箱长度不能超过" + EMAIL_LENGTH + "个字符";

    /**
     * 邮箱验证码为空错误
     */
    public static final String ERROR_EMAIL_CAPTCHA_EMPTY = "邮箱验证码不能为空";

    /**
     * 邮箱已占用错误
     */
    public static final String ERROR_EMAIL_USED = "该邮箱已占用";

    /**
     * 邮件发送失败错误
     */
    public static final String ERROR_EMAIL_SEND = "邮件发送失败";

    /**
     * 邮件验证码校验失败错误
     */
    public static final String ERROR_EMAIL_VERIFY = "邮箱验证码错误或已过期";

    ///////////////////////////////////////////////// 图形验证码 ///////////////////////////////////////////////
    /**
     * 图形验证码错误
     */
    public static final String ERROR_IMAGE_CAPTCHA = "图片验证码错误或已过期";

    /**
     * 图形验证码为空错误
     */
    public static final String ERROR_IMAGE_CAPTCHA_EMPTY = "图片验证码不能为空";

    /**
     * 图形验证码格式错误
     */
    public static final String ERROR_IMAGE_CAPTCHA_FORMAT = "图片验证码格式错误";

    /**
     * 图形验证码ID为空错误
     */
    public static final String ERROR_IMAGE_CAPTCHA_ID_EMPTY = "图片验证码ID不能为空";

    ///////////////////////////////////////////////// 用户名 ///////////////////////////////////////////////
    /**
     * 用户名为空错误
     */
    public static final String ERROR_USERNAME_EMPTY = "用户名不能为空";

    /**
     * 用户名已占用错误
     */
    public static final String ERROR_USERNAME_USED = "该用户名已占用";

    /**
     * 用户名格式错误
     */
    public static final String ERROR_USERNAME_FORMAT = "用户名格式不正确，只允许由" + USERNAME_MIN_LENGTH + "到" + USERNAME_MAX_LENGTH + "位的组合构成，必须以字母开头，其余可以是字母、数字、-或~";

    ///////////////////////////////////////////////// 昵称 ///////////////////////////////////////////////
    /**
     * 昵称为空错误
     */
    public static final String ERROR_NICKNAME_EMPTY = "昵称不能为空";

    /**
     * 昵称长度错误
     */
    public static final String ERROR_NICKNAME_LENGTH = "昵称长度不能超过" + NICKNAME_MAX_LENGTH + "个字符";

    ///////////////////////////////////////////////// 事件 ///////////////////////////////////////////////
    /**
     * 事件为空错误
     */
    public static final String ERROR_EVENT_EMPTY = "事件不能为空";

    /**
     * 事件非法错误
     */
    public static final String ERROR_EVENT = "事件不合法";

    ///////////////////////////////////////////////// 账号 ///////////////////////////////////////////////
    /**
     * 账号或密码错误
     */
    public static final String ERROR_ACCOUNT_OR_PASSWORD = "账号或密码错误";

    /**
     * 账号已禁用
     */
    public static final String ERROR_ACCOUNT_BAN = "账号已禁用";

    /**
     * 账号不存在错误
     */
    public static final String ERROR_ACCOUNT_NO_EXIST = "账号不存在";

    /**
     * 注册失败
     */
    public static final String ERROR_REGISTER = "注册失败";

    /**
     * 重置密码失败
     */
    public static final String ERROR_RESET_PASSWORD = "重置密码失败";

    /**
     * 更新密码失败
     */
    public static final String ERROR_UPDATE_PASSWORD = "更新密码失败";

    /**
     * 存储空间不足
     */
    public static final String ERROR_INSUFFICIENT_STORAGE = "存储空间不足";

    /**
     * 文件大小超出
     */
    public static final String ERROR_FILE_SIZE_OVERFLOW = "文件大小超出";


    ///////////////////////////////////////////////// 文件 ///////////////////////////////////////////////
    /**
     * 文件大小为空错误
     */
    public static final String ERROR_FILE_ID_EMPTY = "文件(夹)ID不能为空";

    /**
     * 文件(夹)不存在
     */
    public static final String ERROR_FILE_NO_EXIST = "文件(夹)不存在";

    /**
     * 文件(夹)名称已存在
     */
    public static final String ERROR_FILE_NAME_EXIST = "文件(夹)名称已存在";

    /**
     * 父文件夹不存在
     */
    public static final String ERROR_PARENT_FOLDER_NO_EXIST = "父文件夹不存在";

    /**
     * 目标文件夹不存在
     */
    public static final String ERROR_TARGET_FOLDER_NO_EXIST = "目标文件夹不存在";

    /**
     * 文件夹新建错误
     */
    public static final String ERROR_FOLDER_CREATE = "新建文件夹失败";

    /**
     * 重命名错误
     */
    public static final String ERROR_RENAME = "重命名失败";

    /**
     * 取消上传失败错误
     */
    public static final String ERROR_CANCEL_UPLOAD = "取消上传失败";

    /**
     * 文件(夹)名称格式错误
     */
    public static final String ERROR_FILE_NAME_FORMAT = "文件(夹)名称不能包含<>|*?/";

    /**
     * 文件(夹)名称长度错误
     */
    public static final String ERROR_FILE_NAME_LENGTH = "文件(夹)名称长度不能超过" + FILE_NAME_MAX_LENGTH;

    /**
     * 文件(夹)名称为空错误
     */
    public static final String ERROR_FILE_NAME_EMPTY = "文件(夹)名称不能为空";

    /**
     * 文件大小为空错误
     */
    public static final String ERROR_FILE_SIZE_EMPTY = "文件大小不能为空";

    /**
     * 文件标识为空错误
     */
    public static final String ERROR_IDENTIFIER_EMPTY = "文件标识不能为空";

    /**
     * 任务ID为空错误
     */
    public static final String ERROR_TASK_ID_EMPTY = "任务ID不能为空";

    /**
     * 切片索引为空错误
     */
    public static final String ERROR_CHUNK_INDEX_EMPTY = "切片索引不能为空";

    /**
     * 总切片数为空错误
     */
    public static final String ERROR_TOTAL_CHUNKS_EMPTY = "总切片数不能为空";

    /**
     * 移动内容为空错误
     */
    public static final String ERROR_MOVE_CONTENT_EMPTY = "请选择要移动的文件(夹)";

    /**
     * 文件正在当前目录，无需移动 错误
     */
    public static final String ERROR_MOVE_IN_CURRENT_FOLDER = "文件正在当前目录，无需移动";

    /**
     * 不能将文件移动到自身或其子目录下 错误
     */
    public static final String ERROR_MOVE_TARGET_IS_ITSELF_OR_SUB = "不能将文件移动到自身或其子目录下";

    /**
     * 移动失败，目标文件夹已有同名文件 错误
     */
    public static final String ERROR_MOVE_TARGET_CONTAIN_SAME_NAME_FILE = "移动失败，目标文件夹已有同名文件";

    /**
     * 移动失败，目标文件夹已有同名文件夹错误
     */
    public static final String ERROR_MOVE_TARGET_CONTAIN_SAME_NAME_FOLDER = "移动失败，目标文件夹已有同名文件夹";

    /**
     * 复制内容为空错误
     */
    public static final String ERROR_COPY_CONTENT_EMPTY = "请选择要复制的文件(夹)";

    /**
     * 文件正在当前目录，无需复制 错误
     */
    public static final String ERROR_COPY_IN_CURRENT_FOLDER = "文件正在当前目录，无需复制";

    /**
     * 不能将文件复制到自身或其子目录下 错误
     */
    public static final String ERROR_COPY_TARGET_IS_ITSELF_OR_SUB = "不能将文件复制到自身或其子目录下";

    /**
     * 复制失败，目标文件夹已有同名文件 错误
     */
    public static final String ERROR_COPY_TARGET_CONTAIN_SAME_NAME_FILE = "复制失败，目标文件夹已有同名文件";

    /**
     * 复制失败，目标文件夹已有同名文件夹错误
     */
    public static final String ERROR_COPY_TARGET_CONTAIN_SAME_NAME_FOLDER = "复制失败，目标文件夹已有同名文件夹";

    /**
     * 删除内容为空错误
     */
    public static final String ERROR_DEL_CONTENT_EMPTY = "请选择要删除的文件(夹)";

    /**
     * 还原内容为空错误
     */
    public static final String ERROR_RESTORE_CONTENT_EMPTY = "请选择要还原的文件(夹)";

    /**
     * 服务器错误
     */
    public static final String ERROR_SERVER = "服务器内部错误";

    /**
     * 事件为空错误
     */
    public static final String ERROR_CATEGORY_EMPTY = "分类不能为空";

    /**
     * 分类不合法错误
     */
    public static final String ERROR_CATEGORY = "分类不合法";

    /**
     * 父文件夹ID为空错误
     */
    public static final String ERROR_PARENT_ID_EMPTY = "父文件夹ID不能为空";

    /**
     * sign非法错误
     */
    public static final String ERROR_SIGN = "sign不合法";
}
