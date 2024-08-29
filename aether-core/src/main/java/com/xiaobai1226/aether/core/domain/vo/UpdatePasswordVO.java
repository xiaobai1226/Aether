package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Pattern;

import static com.xiaobai1226.aether.core.constant.RegexConsts.REGEX_PASSWARD;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_PASSWARD_EMPTY;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_PASSWARD_FORMAT;

/**
 * 修改密码VO
 *
 * @author bai
 */
@Data
public class UpdatePasswordVO {

    /**
     * 密码
     */
    @NotBlank(message = ERROR_PASSWARD_EMPTY)
    @Pattern(value = REGEX_PASSWARD, message = ERROR_PASSWARD_FORMAT)
    private String password;
}
