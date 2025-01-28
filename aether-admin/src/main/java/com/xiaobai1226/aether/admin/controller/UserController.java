package com.xiaobai1226.aether.admin.controller;

import com.xiaobai1226.aether.admin.domain.vo.UserVO;
import com.xiaobai1226.aether.admin.service.intf.UserService;
import com.xiaobai1226.aether.common.domain.dto.Result;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.UserDO;
import com.xiaobai1226.aether.domain.vo.AddUserVO;
import com.xiaobai1226.aether.domain.vo.UpdateUserVO;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import static com.xiaobai1226.aether.common.constant.GateWayTagConsts.API_ADMIN;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultSuccessMsgEnum.*;

/**
 * 用户Controller
 *
 * @author bai
 */
@Component(tag = API_ADMIN)
@Mapping("/user")
@Valid
public class UserController {

    @Inject
    private UserService userService;

    /**
     * 分页获取用户列表
     *
     * @param userVO 用户VO
     */
    @Get
    @Mapping("/getUserListByPage")
    public PageResult<UserDO> getUserListByPage(UserVO userVO) {
        return userService.getUserList(userVO);
    }

    /**
     * 新增用户
     *
     * @param addUserVO 新增用户VO
     */
    @Post
    @Mapping("/addUser")
    public Result addUser(AddUserVO addUserVO) {
        var insertCount = userService.addUser(addUserVO);

        if (insertCount != 1) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        return Result.success(SUCCESS_MSG_CREATE_USER.msg());
    }

    /**
     * 修改用户
     */
    @Post
    @Mapping("/updateUser")
    public Result updateUser(@Validated UpdateUserVO updateUserVO) {

        userService.updateUser(updateUserVO);

        return Result.success(SUCCESS_MSG_UPDATE_USER.msg());
    }
}
