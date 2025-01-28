package com.xiaobai1226.aether.core.domain.vo;

import com.xiaobai1226.aether.core.constant.CaptchaConsts;
import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Pattern;

import static com.xiaobai1226.aether.common.constant.RegexConsts.REGEX_PASSWARD;
import static com.xiaobai1226.aether.common.constant.RegexConsts.REGEX_USERNAME;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;

/**
 * 登录VO
 *
 * @author bai
 */
@Data
public class LoginVO {

    /**
     * 用户名
     */
    @NotBlank(message = ERROR_USERNAME_EMPTY)
    @Pattern(value = REGEX_USERNAME, message = ERROR_USERNAME_FORMAT)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = ERROR_PASSWARD_EMPTY)
    @Pattern(value = REGEX_PASSWARD, message = ERROR_PASSWARD_FORMAT)
    private String password;

    /**
     * 验证码ID
     */
    @NotBlank(message = ERROR_IMAGE_CAPTCHA_ID_EMPTY)
    private String captchaId;

    /**
     * 用户输入的图片验证码
     */
    @NotBlank(message = ERROR_IMAGE_CAPTCHA_EMPTY)
    @Length(min = CaptchaConsts.IMAGE_CAPTCHA_LENGTH, max = CaptchaConsts.IMAGE_CAPTCHA_LENGTH, message = ERROR_IMAGE_CAPTCHA_FORMAT)
    private String captchaCode;
}
