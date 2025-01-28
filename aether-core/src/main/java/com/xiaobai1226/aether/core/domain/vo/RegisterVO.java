package com.xiaobai1226.aether.core.domain.vo;

import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Pattern;

import static com.xiaobai1226.aether.common.constant.RegexConsts.REGEX_PASSWARD;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.constant.VerifyConsts.NICKNAME_MAX_LENGTH;

/**
 * 注册VO
 *
 * @author bai
 */
@Data
//@ImageCaptchaVerify(captchaCode = "imageCaptchaCode", captchaId = "imageCaptchaId", message = ERROR_IMAGE_CAPTCHA)
public class RegisterVO {

    /**
     * 邮箱
     */
//    @NotBlank(message = ERROR_EMAIL_EMPTY)
//    @Email(message = ERROR_EMAIL_FORMAT)
//    @Length(max = EMAIL_LENGTH, message = ERROR_EMAIL_LENGTH)
//    private String email;

    /**
     * 用户名
     */
    @NotBlank(message = ERROR_NICKNAME_EMPTY)
    @Length(max = NICKNAME_MAX_LENGTH, message = ERROR_NICKNAME_LENGTH)
    private String username;

    /**
     * 昵称
     */
//    @NotBlank(message = ERROR_NICKNAME_EMPTY)
//    @Length(max = NICKNAME_MAX_LENGTH, message = ERROR_NICKNAME_LENGTH)
//    private String nickname;

    /**
     * 密码
     */
    @NotBlank(message = ERROR_PASSWARD_EMPTY)
    @Pattern(value = REGEX_PASSWARD, message = ERROR_PASSWARD_FORMAT)
    private String password;

    /**
     * 验证码ID
     */
//    @NotBlank(message = ERROR_IMAGE_CAPTCHA_ID_EMPTY)
//    private String imageCaptchaId;

    /**
     * 用户输入的图片验证码
     */
//    @NotBlank(message = ERROR_IMAGE_CAPTCHA_EMPTY)
//    private String imageCaptchaCode;

    /**
     * 用户输入的邮箱验证码
     */
//    @NotBlank(message = ERROR_EMAIL_CAPTCHA_EMPTY)
//    private String emailCaptchaCode;
}
