package com.xiaobai1226.aether.core.domain.vo;

import com.xiaobai1226.aether.core.annotation.EnumValid;
import com.xiaobai1226.aether.core.enums.EmailEventEnum;
import lombok.Data;
import org.noear.solon.validation.annotation.Email;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_EMAIL_LENGTH;
import static com.xiaobai1226.aether.core.constant.VerifyConsts.EMAIL_LENGTH;

/**
 * 发送邮箱验证码VO
 *
 * @author bai
 */
@Data
//@ImageCaptchaVerify(captchaCode = "captchaCode", captchaId = "captchaId", message = ERROR_IMAGE_CAPTCHA)
public class SendEmailCodeVO {

    /**
     * 邮箱
     */
    @NotBlank(message = ERROR_EMAIL_EMPTY)
    @Email(message = ERROR_EMAIL_FORMAT)
    @Length(max = EMAIL_LENGTH, message = ERROR_EMAIL_LENGTH)
    private String email;

    /**
     * 验证码ID
     */
    @NotBlank(message = ERROR_IMAGE_CAPTCHA_ID_EMPTY)
    private String captchaId;

    /**
     * 用户输入的验证码
     */
    @NotBlank(message = ERROR_EMAIL_CAPTCHA_EMPTY)
    private String captchaCode;

    /**
     * 行为事件 0 注册 1 登录
     */
    @NotNull(message = ERROR_EVENT_EMPTY)
    @EnumValid(target = EmailEventEnum.class, message = ERROR_EVENT, vaildField = "eventId")
    private Integer event;
}
