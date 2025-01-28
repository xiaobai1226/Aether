package com.xiaobai1226.aether.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体类DO
 *
 * @author bai
 */
@TableName("user")
@Data
public class UserDO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
//    private String email;

    /**
     * 头像
     */
//    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色ID 0 普通用户 1 超级管理员
     */
    private Integer roleId;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 最近一次登录时间
     */
    private String lastLoginTime;

    /**
     * 用户状态 0 : 禁用；1 ：启用
     */
    private Integer status;

    /**
     * 已使用存储空间 单位 byte
     */
    private Long usedStorage;

    /**
     * 总存储空间 单位 byte
     */
    private Long totalStorage;
}