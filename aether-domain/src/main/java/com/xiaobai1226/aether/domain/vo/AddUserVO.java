package com.xiaobai1226.aether.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Pattern;

import static com.xiaobai1226.aether.common.constant.RegexConsts.REGEX_PASSWARD;
import static com.xiaobai1226.aether.common.constant.RegexConsts.REGEX_USERNAME;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_PASSWARD_FORMAT;

/**
 * 新增用户VO
 *
 * @author bai
 */
@Data
public class AddUserVO {

    /**
     * 用户名
     */
    @NotBlank(message = ERROR_USERNAME_EMPTY)
    @Pattern(value = REGEX_USERNAME, message = ERROR_USERNAME_FORMAT)
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码
     */
    @NotBlank(message = ERROR_PASSWARD_EMPTY)
    @Pattern(value = REGEX_PASSWARD, message = ERROR_PASSWARD_FORMAT)
    private String password;

    /**
     * 密码
     */
    private Integer roleId;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 总存储空间 单位 byte
     */
    private Long totalStorage;
}
