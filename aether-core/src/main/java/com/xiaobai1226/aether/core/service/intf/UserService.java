package com.xiaobai1226.aether.core.service.intf;

import com.xiaobai1226.aether.core.domain.dto.UserSpaceUsageDTO;
import com.xiaobai1226.aether.core.domain.entity.UserDO;
import com.xiaobai1226.aether.core.domain.vo.RegisterVO;

/**
 * 用户服务接口
 *
 * @author bai
 */
public interface UserService {

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     * @author bai
     */
    UserDO getUserByUsername(String username);

    /**
     * 更新登录时间
     *
     * @param userId 用户ID
     * @author bai
     */
    void updateUserLastLoginTime(Integer userId);

    /**
     * 查询用户空间使用情况
     *
     * @param userId 用户ID
     * @return 用户空间使用情况
     */
    UserSpaceUsageDTO getUserSpaceUsage(Integer userId);

    /**
     * 修改用户
     *
     * @param userDO 用户信息
     * @return 修改结果的条数
     * @author bai
     */
    Integer updateUser(UserDO userDO);

    /**
     * 根据用户ID更新密码
     *
     * @param id       用户ID
     * @param password 密码
     * @return 修改结果的条数
     * @author bai
     */
    Integer updatePasswordById(Integer id, String password);






    /**
     * 根据邮箱获取用户数量
     *
     * @param email 邮箱
     * @return 返回查询的结果数量
     * @author bai
     */
    Long getUserCountByEmail(String email);

    /**
     * 根据用户名获取用户数量
     *
     * @param username 用户名
     * @return 返回查询的结果数量
     * @author bai
     */
//    Long getUserCountByUsername(String username);

    /**
     * 新增用户
     *
     * @param registerVO 注册参数
     * @return 修改结果的条数
     * @author bai
     */
    Integer addUser(RegisterVO registerVO);

    /**
     * 根据邮箱更新密码
     *
     * @param email    邮箱
     * @param password 密码
     * @return 修改结果的条数
     * @author bai
     */
    Integer updatePasswordByEmail(String email, String password);

    /**
     * 更新用户已使用存储空间
     *
     * @param userId      用户ID
     * @param usedStorage 已使用存储空间
     * @return 修改结果的条数
     */
    Integer updateUsedStorage(Integer userId, Long usedStorage);

    /**
     * 更新用户总存储空间
     *
     * @param userId       用户ID
     * @param totalStorage 总存储空间
     * @return 修改结果的条数
     */
    Integer updateTotalStorage(Integer userId, Long totalStorage);
}
