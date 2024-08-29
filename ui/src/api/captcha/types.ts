/**
 * 图形验证码信息
 */
export interface ImageCaptchaInfo {
    /**
     * 图形验证码ID
     */
    captchaId: string;

    /**
     * 图形验证码base64数据
     */
    captchaBase64Data: string;
}