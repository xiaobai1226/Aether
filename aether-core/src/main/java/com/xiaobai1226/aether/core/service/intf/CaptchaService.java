package com.xiaobai1226.aether.core.service.intf;

import com.xiaobai1226.aether.core.domain.dto.ImageCaptchaDTO;

/**
 * 验证码服务接口
 *
 * @author bai
 */
public interface CaptchaService {

    /**
     * 获取图形验证码
     *
     * @author bai
     */
    ImageCaptchaDTO getImageCaptcha();

    /**
     * 校验图形验证码
     *
     * @param captchaId     验证码ID
     * @param userInputCode 用户输入的验证码
     * @return 验证码校验结果 true通过 false 不通过
     * @author bai
     */
    Boolean verifyImageCaptcha(String captchaId, String userInputCode);

    /**
     * 发送邮箱验证码
     *
     * @param event 事件
     * @param email 邮箱
     * @return 发送验证码结果 true通过 false 不通过
     * @author bai
     */
//    Boolean sendEmailCaptcha(EmailEventEnum event, String email);

    /**
     * 校验邮箱验证码
     *
     * @param event         事件
     * @param email         邮箱
     * @param userInputCode 用户输入的验证码
     * @return 验证码校验结果 true通过 false 不通过
     * @author bai
     */
//    Boolean verifyEmailCaptcha(EmailEventEnum event, String email, String userInputCode);

    /**
     * 删除邮箱验证码数据
     *
     * @param event 事件
     * @param email 邮箱
     * @return 删除结果 true 删除成功 false 删除失败
     */
//    Boolean delEmailCaptcha(EmailEventEnum event, String email);
}