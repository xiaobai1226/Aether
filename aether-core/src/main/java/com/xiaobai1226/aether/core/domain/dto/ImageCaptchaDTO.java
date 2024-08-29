package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 图形验证码DTO
 *
 * @author bai
 */
@Data
@AllArgsConstructor
public class ImageCaptchaDTO {

    /**
     * 图形验证码ID
     */
    public String captchaId;

    /**
     * 图形验证码base64数据
     */
    public String captchaBase64Data;
}
