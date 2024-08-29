package com.xiaobai1226.aether.core.enums;

/**
 * 成功结果描述枚举类
 *
 * @author bai
 */
public enum ResultSuccessMsgEnum {
    SUCCESS_MSG_LOGIN("登录成功"),
    SUCCESS_MSG_CREATE_FOLDER("新建文件夹成功"),
    SUCCESS_MSG_MOVE("移动成功"),
    SUCCESS_MSG_COPY("复制成功"),
    SUCCESS_MSG_DELETE("删除成功"),
    SUCCESS_MSG_RENAME("重命名成功"),
    SUCCESS_MSG_RECYCLE_BIN_DELETE("删除成功"),
    SUCCESS_MSG_RECYCLE_BIN_RESTORE("还原成功"),
    SUCCESS_MSG_CREATE_SHARE("分享成功"),
    SUCCESS_MSG_CANCEL_SHARE("取消分享成功"),
    SUCCESS_MSG_UPDATE_PASSWORD("修改密码成功"),
    SUCCESS_MSG_UPDATE_AVATAR("修改头像成功"),
    SUCCESS_MSG_CANCEL_UPLOAD("取消上传成功"),
    ;

    private final String msg;

    ResultSuccessMsgEnum(String msg) {
        this.msg = msg;
    }

    public String msg() {
        return this.msg;
    }
}