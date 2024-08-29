package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.core.constant.CaptchaConsts;
import com.xiaobai1226.aether.core.domain.dto.ImageCaptchaDTO;
import com.xiaobai1226.aether.core.dao.redis.CaptchaRedisDAO;
import com.xiaobai1226.aether.core.service.intf.CaptchaService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * 验证码服务实现类
 *
 * @author bai
 */
//@Component("captchaService")
@Component
public class CaptchaServiceImpl implements CaptchaService {

    /**
     * 图形验证码Redis缓存
     */
    @Inject
    private CaptchaRedisDAO captchaRedisDAO;

//    @Inject
//    private EmailService emailService;

    /**
     * 获取图形验证码
     *
     * @author bai
     */
    @Override
    public ImageCaptchaDTO getImageCaptcha() {
        // 定义图形验证码的长、宽、验证码字符数、干扰元素个数
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(CaptchaConsts.IMAGE_CAPTCHA_WIDTH, CaptchaConsts.IMAGE_CAPTCHA_HEIGHT, CaptchaConsts.IMAGE_CAPTCHA_LENGTH, CaptchaConsts.IMAGE_CAPTCHA_CIRCLE_COUNT);
        // 生成code
        captcha.createCode();
        // 生成uuid
        var captchaId = IdUtil.simpleUUID();
        // 加入缓存
        captchaRedisDAO.setImageCaptcha(captchaId, captcha.getCode(), CaptchaConsts.IMAGE_CAPTCHA_TIMEOUT);

        return new ImageCaptchaDTO(captchaId, captcha.getImageBase64Data());
    }

    @Override
    public Boolean verifyImageCaptcha(String captchaId, String userInputCode) {
        String captchaCode = captchaRedisDAO.getAndDelImageCaptcha(captchaId);

        if (StrUtil.isNotEmpty(captchaCode)) {
            return StrUtil.equalsIgnoreCase(captchaCode, userInputCode);
        }
        return false;
    }

//    @Override
//    public Boolean sendEmailCaptcha(EmailEventEnum event, String email) {
//
//        // 生成验证码
//        String captchaCode = RandomUtil.randomStringUpper(CaptchaConsts.EMAIL_CAPTCHA_LENGTH);
//
//        // 发送邮件
//        var sendResult = emailService.sendHtmlMail(email, CaptchaConsts.EMAIL_CAPTCHA_SUBJECT, String.format(CaptchaConsts.EMAIL_CAPTCHA_CONTENT, captchaCode));
//
//        if (sendResult) {
//            // 加入缓存
//            captchaRedisDAO.setEmailCaptcha(EmailEventEnum.EVENT_REGISTER, email, captchaCode, CaptchaConsts.EMAIL_CAPTCHA_TIMEOUT);
//        }
//
//        return sendResult;
//    }

//    @Override
//    public Boolean verifyEmailCaptcha(EmailEventEnum event, String email, String userInputCode) {
//        String captchaCode = captchaRedisDAO.getEmailCaptcha(event, email);
//
//        if (StrUtil.isNotEmpty(captchaCode)) {
//            return StrUtil.equalsIgnoreCase(captchaCode, userInputCode);
//        }
//        return false;
//    }

//    @Override
//    public Boolean delEmailCaptcha(EmailEventEnum event, String email) {
//        return captchaRedisDAO.delEmailCaptcha(event, email);
//    }
}
