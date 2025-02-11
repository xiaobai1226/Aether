package com.xiaobai1226.aether.core.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.solon.conditions.query.LambdaQueryChainWrapper;
import com.xiaobai1226.aether.core.dao.redis.UserRedisDAO;
import com.xiaobai1226.aether.core.domain.dto.UserSpaceUsageDTO;
import com.xiaobai1226.aether.domain.entity.UserDO;
import com.xiaobai1226.aether.core.domain.vo.RegisterVO;
import com.xiaobai1226.aether.dao.mapper.UserMapper;
import com.xiaobai1226.aether.core.service.intf.UserService;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import static com.xiaobai1226.aether.common.constant.SystemConsts.DEFAULT_USER_TOTAL_SPACE;
import static com.xiaobai1226.aether.common.enums.UserStatusEnum.NORMAL;

/**
 * 用户service实现类
 *
 * @author bai
 */
@Component
public class UserServiceImpl implements UserService {

    @Db
    private UserMapper userMapper;

    @Inject
    private UserRedisDAO userRedisDAO;

    @Override
    public UserDO getUserByUsername(String username) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(userMapper);
        return lambdaQuery.eq(UserDO::getUsername, username).one();
    }

    @Override
    public void updateUserLastLoginTime(Integer userId) {
        var lambdaUpdate = new LambdaUpdateWrapper<UserDO>();
        lambdaUpdate.eq(UserDO::getId, userId).set(UserDO::getLastLoginTime, DateUtil.now());
        userMapper.update(null, lambdaUpdate);
    }

    @Override
    public UserSpaceUsageDTO getUserSpaceUsage(Integer userId) {
        var userDO = userMapper.selectById(userId);
        var remainStorage = userDO.getTotalStorage() - userDO.getUsedStorage();
        var uploadingUsedStorage = userRedisDAO.getUploadingFileSize(userId);
        var realRemainStorage = remainStorage - uploadingUsedStorage;
        return new UserSpaceUsageDTO(userDO.getUsedStorage(), userDO.getTotalStorage(), remainStorage, uploadingUsedStorage, realRemainStorage);
    }

    @Override
    public Integer updateUser(UserDO userDO) {
        return userMapper.updateById(userDO);
    }

    @Override
    public Integer updatePasswordById(Integer id, String password) {
        var lambdaUpdate = new LambdaUpdateWrapper<UserDO>();
        lambdaUpdate.eq(UserDO::getId, id).set(UserDO::getPassword, BCrypt.hashpw(password));
        return userMapper.update(null, lambdaUpdate);
    }






    @Override
    public Long getUserCountByEmail(String email) {
//        var lambdaQuery = new LambdaQueryChainWrapper<>(userMapper);
//        return lambdaQuery.eq(UserDO::getEmail, email).count();
        return 0L;
    }

//    @Override
//    public Long getUserCountByUsername(String username) {
//        var lambdaQuery = new LambdaQueryChainWrapper<>(userMapper);
//        return lambdaQuery.eq(UserDO::getUsername, username).count();
//    }

    @Override
    public Integer addUser(RegisterVO registerVO) {
        var userDO = BeanUtil.toBean(registerVO, UserDO.class);
        userDO.setPassword(BCrypt.hashpw(registerVO.getPassword()));
        userDO.setStatus(NORMAL.flag());
        // TODO 改为从数据库或缓存中获取
        userDO.setTotalStorage(DEFAULT_USER_TOTAL_SPACE);
        // TODO 改为从数据库获取
//        userDO.setRoleId(0);
        return userMapper.insert(userDO);
    }

    @Override
    public Integer updatePasswordByEmail(String email, String password) {
//        var lambdaUpdate = new LambdaUpdateWrapper<UserDO>();
//        lambdaUpdate.eq(UserDO::getEmail, email).set(UserDO::getUserPassword, BCrypt.hashpw(password));
//        return userMapper.update(null, lambdaUpdate);
        return 0;
    }

    @Override
    public Integer updateUsedStorage(Integer userId, Long usedStorage) {
        // TODO 确认好我到底使不使用这个字段后再开发
        return null;
    }

    @Override
    public Integer updateTotalStorage(Integer userId, Long totalStorage) {
        return null;
    }
}

