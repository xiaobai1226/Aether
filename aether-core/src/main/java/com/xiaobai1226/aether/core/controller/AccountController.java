package com.xiaobai1226.aether.core.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xiaobai1226.aether.core.annotation.ImageCaptchaVerify;
import com.xiaobai1226.aether.core.domain.dto.LoginUserInfoDTO;
import com.xiaobai1226.aether.core.domain.vo.LoginVO;
import com.xiaobai1226.aether.core.domain.vo.RegisterVO;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.AccountService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.common.domain.dto.Result;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import static com.xiaobai1226.aether.common.constant.GateWayTagConsts.API_V1;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultSuccessMsgEnum.SUCCESS_MSG_LOGIN;

/**
 * 账户Controller
 *
 * @author bai
 */
@Component(tag = API_V1)
@Valid
public class AccountController {

//    @Inject
//    private CaptchaService captchaService;

    @Inject
    private UserService userService;

    @Inject
    private AccountService accountService;

    /**
     * 注册
     * TODO 待完成
     *
     * @param registerVO 注册相关参数
     * @author bai
     */
    @Post
    @Mapping("/register")
    public void register(@Body @Validated RegisterVO registerVO) {

        // 根据邮箱查询用户数量
//        var emailUserCount = userService.getUserCountByEmail(registerVO.getEmail());
//        if (emailUserCount > 0) {
//            throw new FailResultException(PARAM_IS_INVALID, ERROR_EMAIL_USED);
//        }

        // 校验邮箱验证码
//        var emailVerifyResult = captchaService.verifyEmailCaptcha(EVENT_REGISTER, registerVO.getEmail(), registerVO.getEmailCaptchaCode());
//        if (!emailVerifyResult) {
//            throw new FailResultException(PARAM_IS_INVALID, ERROR_EMAIL_VERIFY);
//        }

        var addUserCount = userService.addUser(registerVO);

        if (addUserCount != 1) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_REGISTER);
        } else {
            // 成功后删除缓存
//            captchaService.delEmailCaptcha(EVENT_REGISTER, registerVO.getEmail());
        }
    }

    /**
     * 登录
     *
     * @param loginVO 登录相关参数
     * @return 登录信息
     * @author bai
     */
    @Post
    @Mapping("/login")
    @ImageCaptchaVerify(captchaCode = "captchaCode", captchaId = "captchaId", message = ERROR_IMAGE_CAPTCHA)
    public Result<LoginUserInfoDTO> login(@Validated LoginVO loginVO) {
        var LoginUserInfoDTO = accountService.login(loginVO.getUsername(), loginVO.getPassword());
        return Result.success(SUCCESS_MSG_LOGIN.msg(), LoginUserInfoDTO);
    }

    /**
     * 退出登录
     *
     * @author bai
     */
    @Delete
    @Mapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success("退出登录成功");
    }

    /**
     * 重置密码
     * TODO 删除邮箱字段后，这部分逻辑要重写
     *
     * @param resetPasswordVO 重置密码相关参数
     * @author bai
     */
//    @Post
//    @Mapping("/resetPassword")
//    public void resetPassword(@Body @Validated ResetPasswordVO resetPasswordVO) {
//
//        // 根据邮箱查询用户数量
//        var emailUserCount = userService.getUserCountByEmail(resetPasswordVO.getEmail());
//        if (emailUserCount <= 0) {
//            throw new FailResultException(PARAM_IS_INVALID, ERROR_ACCOUNT_NO_EXIST);
//        }
//
//        // 校验邮箱验证码
//        var emailVerifyResult = captchaService.verifyEmailCaptcha(EVENT_RESET_PASSWORD, resetPasswordVO.getEmail(), resetPasswordVO.getEmailCaptchaCode());
//        if (!emailVerifyResult) {
//            throw new FailResultException(PARAM_IS_INVALID, ERROR_EMAIL_VERIFY);
//        }
//
//        var resultCount = userService.updatePasswordByEmail(resetPasswordVO.getEmail(), resetPasswordVO.getPassword());
//
//        if (resultCount != 1) {
//            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_RESET_PASSWORD);
//        } else {
//            // 成功后删除缓存
//            captchaService.delEmailCaptcha(EVENT_RESET_PASSWORD, resetPasswordVO.getEmail());
//        }
//    }
}