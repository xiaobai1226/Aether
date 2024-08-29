package com.xiaobai1226.aether.core.controller;

import com.xiaobai1226.aether.core.domain.dto.ImageCaptchaDTO;
import com.xiaobai1226.aether.core.service.intf.CaptchaService;
import org.noear.solon.annotation.*;

import static com.xiaobai1226.aether.core.constant.GateWayTagConsts.API_V1;

/**
 * 验证码Controller
 *
 * @author bai
 */
@Component(tag = API_V1)
@Mapping("/captcha")
public class CaptchaController {

    @Inject
    private CaptchaService captchaService;

//    @Inject
//    private UserService userService;

    /**
     * 获取图形验证码
     *
     * @return 结果的json字符串
     * @author bai
     */
    @Get
    @Mapping("/getImageCaptcha")
    public ImageCaptchaDTO getImageCaptcha() {
        return captchaService.getImageCaptcha();
    }

    /**
     * 发送邮箱验证码
     *
     * @author bai
     */
//    @Post
//    @Mapping("/sendEmailCaptcha")
//    public void sendEmailCaptcha(@Body @Validated SendEmailCodeVO sendEmailCodeVO) {
//
//        // 获取事件枚举对象
//        var eventEnum = EnumUtil.getBy(EmailEventEnum::eventId, sendEmailCodeVO.getEvent());
//        if (eventEnum == null) {
//            throw new FailResultException(PARAM_IS_INVALID, ERROR_EVENT);
//        }
//
//        // 检验该邮箱是否已占用
//        var userCount = userService.getUserCountByEmail(sendEmailCodeVO.getEmail());
//        if (userCount > 0) {
//            throw new FailResultException(PARAM_IS_INVALID, ERROR_EMAIL_USED);
//        }
//
//        // 发送邮箱验证码
//        Boolean result = captchaService.sendEmailCaptcha(eventEnum, sendEmailCodeVO.getEmail());
//
//        if (!result) {
//            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_EMAIL_SEND);
//        }
//    }
}
