package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;

/**
 * 登录用户信息DTO
 *
 * @author bai
 */
@Data
public class LoginUserInfoDTO {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 已使用存储空间 单位 byte
     */
    private Long usedStorage;

    /**
     * 总存储空间 单位 byte
     */
    private Long totalStorage;

    /**
     * 角色ID
     */
    private Integer roleId;

    /**
     * 登录token
     */
    private String token;

    /**
     * 登录token名称
     */
    private String tokenName;

    /**
     * 登录前缀
     */
    private String tokenPrefix;
}
