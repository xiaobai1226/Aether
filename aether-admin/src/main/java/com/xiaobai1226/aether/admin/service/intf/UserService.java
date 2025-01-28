package com.xiaobai1226.aether.admin.service.intf;

import com.xiaobai1226.aether.admin.domain.vo.UserVO;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.UserDO;
import com.xiaobai1226.aether.domain.vo.AddUserVO;
import com.xiaobai1226.aether.domain.vo.UpdateUserVO;

/**
 * 用户服务接口
 *
 * @author bai
 */
public interface UserService {

    /**
     * 获取用户列表
     *
     * @param userVO 用户信息
     * @return 获取到的用户数据
     * @author bai
     */
    PageResult<UserDO> getUserList(UserVO userVO);

    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     * @author bai
     */
    UserDO getUserById(Integer id);

    /**
     * 新增用户
     *
     * @param addUserVO 新增用户参数
     * @return 修改结果的条数
     * @author bai
     */
    Integer addUser(AddUserVO addUserVO);

    /**
     * 修改用户
     *
     * @param updateUserVO 用户信息
     * @return 修改结果的条数
     * @author bai
     */
    Integer updateUser(UpdateUserVO updateUserVO);
}
