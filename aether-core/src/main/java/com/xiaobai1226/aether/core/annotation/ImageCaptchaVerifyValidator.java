package com.xiaobai1226.aether.core.annotation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiaobai1226.aether.core.service.intf.CaptchaService;
import org.noear.solon.Solon;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.Validator;

import java.io.IOException;

import static com.xiaobai1226.aether.core.constant.CaptchaConsts.IMAGE_CAPTCHA_LENGTH;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_IMAGE_CAPTCHA_EMPTY;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_IMAGE_CAPTCHA_ID_EMPTY;

/**
 * 图片验证验证码是否正确 验证器
 *
 * @author bai
 */
public class ImageCaptchaVerifyValidator implements Validator<ImageCaptchaVerify> {

    public static final ImageCaptchaVerifyValidator instance = new ImageCaptchaVerifyValidator();

    @Override
    public String message(ImageCaptchaVerify anno) {
        return anno.message();
    }

    /**
     * 校验上下文的参数（注解在函数上时用到）
     */
    @Override
    public Result validateOfContext(Context ctx, ImageCaptchaVerify anno, String name, StringBuilder tmp) {
        try {
            var body = ctx.body();
            if (body == null || body.isEmpty()) {
                return Result.failure(name);
            }

            JSONObject jsonObject = JSONUtil.parseObj(body);
            var captchaId = jsonObject.getStr(anno.captchaId());
            var captchaCode = jsonObject.getStr(anno.captchaCode());

            if (StrUtil.isEmpty(captchaId)) {
                return Result.failure(ERROR_IMAGE_CAPTCHA_ID_EMPTY);
            } else if (StrUtil.isEmpty(captchaCode)) {
                return Result.failure(ERROR_IMAGE_CAPTCHA_EMPTY);
            } else if (captchaCode.length() != IMAGE_CAPTCHA_LENGTH) {
                return Result.failure();
            }

            if (verify(captchaId, captchaCode)) {
                return Result.succeed();
            }
        } catch (IOException e) {
        }

        return Result.failure();
    }

    /**
     * 校验方法
     *
     * @param captchaId   验证码ID
     * @param captchaCode 验证码
     * @return 校验结果
     */
    public boolean verify(String captchaId, String captchaCode) {
        //同步方式，根据bean type获取Bean（如果此时不存在，则返回null。需要注意时机）
        var captchaService = Solon.context().getBean(CaptchaService.class);
        return captchaService.verifyImageCaptcha(captchaId, captchaCode);
    }
}
