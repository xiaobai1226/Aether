package com.xiaobai1226.aether.admin.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.xiaobai1226.aether.admin.service.intf.UserService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

/**
 * 自定义权限加载接口实现类
 *
 * @author bai
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Inject
    private UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询权限
//        List<String> list = new ArrayList<String>();
//        list.add("101");
//        list.add("user.add");
//        list.add("user.update");
//        list.add("user.get");
//        // list.add("user.delete");
//        list.add("art.*");
//        return list;

        return null;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {

        var user = userService.getUserById(Integer.valueOf(String.valueOf(loginId)));

        if (user.getRoleId() != null && user.getRoleId() == 1) {
            return List.of("admin");
        }

        return null;
    }
}
