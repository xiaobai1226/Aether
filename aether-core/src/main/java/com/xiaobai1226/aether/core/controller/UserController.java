package com.xiaobai1226.aether.core.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.constant.SystemConsts;
import com.xiaobai1226.aether.core.domain.dto.UserSpaceUsageDTO;
import com.xiaobai1226.aether.core.domain.vo.UpdatePasswordVO;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.common.domain.dto.Result;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.Path;

import static com.xiaobai1226.aether.common.constant.GateWayTagConsts.API_V1;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_UPDATE_PASSWORD;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultSuccessMsgEnum.SUCCESS_MSG_UPDATE_AVATAR;
import static com.xiaobai1226.aether.common.enums.ResultSuccessMsgEnum.SUCCESS_MSG_UPDATE_PASSWORD;

/**
 * 用户Controller
 *
 * @author bai
 */
@Component(tag = API_V1)
@Mapping("/user")
@Valid
public class UserController {

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private UserService userService;

    /**
     * 修改密码
     */
    @Post
    @Mapping("/updateUserPassword")
    public Result updateUserPassword(@Validated UpdatePasswordVO updatePasswordVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        var resultCount = userService.updatePasswordById(userId, updatePasswordVO.getPassword());

        if (resultCount != 1) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_UPDATE_PASSWORD);
        }

        return Result.success(SUCCESS_MSG_UPDATE_PASSWORD.msg());
    }

    /**
     * 获取头像
     *
     * @author bai
     */
    @Get
    @Mapping("/getAvatar")
    public void getAvatar(Context ctx) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginId();

        // 头像目录
        final var avatarFolderPath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_AVATAR_FILE_FULL);

        // 如果文件夹不存在则创建文件夹
        if (!FileUtil.exists(Path.of(avatarFolderPath), false)) {
            FileUtil.mkdir(avatarFolderPath);
        }

        // 设置头像存储全路径
        var avatarPath = FileUtils.generatePath(avatarFolderPath, userId + SystemConsts.AVATAR_SUFFIX);
//        ctx.contentType("image/jpg");
        // 判断文件是否存在
//        if (FileUtil.exist(avatarPath)) {
//            FileUtils.readFile(ctx, avatarPath);
//        } else {
//            var defaultAvatarPath = FileUtils.generatePath("/avatar", SystemConsts.DEFAULT_AVATAR_FILE_NAME);
//            FileUtils.readFileByResources(ctx, defaultAvatarPath);
//        }

        try {
            // 判断文件是否存在
            if (!FileUtil.exist(avatarPath)) {
                avatarPath = FileUtils.generatePath("/avatar", SystemConsts.DEFAULT_AVATAR_FILE_NAME);
            }

            var file = FileUtil.file(avatarPath);

            var downloadedFile = new DownloadedFile(file);

            // 不做为附件下载（按需配置）
            downloadedFile.asAttachment(false);

            //也可用接口输出
            ctx.outputAsFile(downloadedFile);
        } catch (IOException e) {
            throw new FailResultException(SYSTEM_ERROR);
        }
    }


    /**
     * 更新头像
     */
    @Post
    @Mapping(path = "/updateUserAvatar")
    public Result updateUserAvatar(UploadedFile avatar) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginId();

        final var avatarFolderPath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_AVATAR_FILE_FULL);

        // 如果文件夹不存在则创建文件夹
        if (!FileUtil.exists(Path.of(avatarFolderPath), false)) {
            FileUtil.mkdir(avatarFolderPath);
        }

        // 设置头像存储全路径
        var avatarPath = FileUtils.generatePath(avatarFolderPath, userId + SystemConsts.AVATAR_SUFFIX);

        var avatarFile = FileUtil.file(avatarPath);

        try {
            avatar.transferTo(avatarFile);
        } catch (IOException e) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        return Result.success(SUCCESS_MSG_UPDATE_AVATAR.msg());
    }

    /**
     * 获取空间使用情况
     */
    @Get
    @Mapping("/getUserSpaceUsage")
    public UserSpaceUsageDTO getUserSpaceUsage() {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        return userService.getUserSpaceUsage(userId);
    }

    /**
     * 获取用户信息
     */
    @Get
    @Mapping("/getUserInfo")
    public void getUserInfo() {
        // TODO 待完成，视频里感觉不太符合自己要求
    }
}
