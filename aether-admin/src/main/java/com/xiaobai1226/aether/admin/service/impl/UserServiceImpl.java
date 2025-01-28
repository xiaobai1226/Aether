package com.xiaobai1226.aether.admin.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.solon.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.solon.plugins.pagination.Page;
import com.xiaobai1226.aether.admin.domain.vo.UserVO;
import com.xiaobai1226.aether.admin.service.intf.UserService;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.dao.mapper.UserMapper;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.UserDO;
import com.xiaobai1226.aether.domain.vo.AddUserVO;
import com.xiaobai1226.aether.domain.vo.UpdateUserVO;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;

import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;

/**
 * 用户service实现类
 *
 * @author bai
 */
@Component
public class UserServiceImpl implements UserService {
    @Db
    private UserMapper userMapper;

    @Override
    public PageResult<UserDO> getUserList(UserVO userVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userMapper);
        // 指定查询字段
        lambdaQuery.select(UserDO::getId, UserDO::getUsername, UserDO::getNickname, UserDO::getRoleId, UserDO::getCreateTime, UserDO::getUpdateTime, UserDO::getLastLoginTime, UserDO::getStatus, UserDO::getUsedStorage, UserDO::getTotalStorage);
//                .eq(UserDO::getUserId, userId).eq(UserDO::getRoot, 1);
//        if (userVO.getSortField() == 2 && userVO.getSortOrder() == 1) {
//            lambdaQuery.orderByDesc(UserDO::getCreateTime);
//        } else if (userVO.getSortField() == 2 && userVO.getSortOrder() == 2) {
//            lambdaQuery.orderByDesc(UserDO::getCreateTime);
//        }

        var userListPage = lambdaQuery.page(new Page<>(userVO.getPageNum(), userVO.getPageSize()));

        // 判断结果是否为空
        if (userListPage == null || CollUtil.isEmpty(userListPage.getRecords())) {
            return null;
        }

        return PageResult.with(userListPage);
    }

    @Override
    public UserDO getUserById(Integer id) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userMapper);
        return lambdaQuery.eq(UserDO::getId, id).one();
    }

    @Override
    public Integer addUser(AddUserVO addUserVO) {
        var userDO = BeanUtil.toBean(addUserVO, UserDO.class);
        userDO.setPassword(BCrypt.hashpw(addUserVO.getPassword()));
        return userMapper.insert(userDO);
    }

    @Override
    public Integer updateUser(UpdateUserVO updateUserVO) {
        var userDO = BeanUtil.toBean(updateUserVO, UserDO.class);
        if (userDO.getPassword() != null) {
            userDO.setPassword(BCrypt.hashpw(updateUserVO.getPassword()));
        }

        var resultCount = userMapper.updateById(userDO);

        if (resultCount != 1) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        if (userDO.getPassword() != null || (userDO.getStatus() != null && userDO.getStatus() == 0)) {
            StpUtil.logout(updateUserVO.getId());
        }

        return resultCount;
    }
}
