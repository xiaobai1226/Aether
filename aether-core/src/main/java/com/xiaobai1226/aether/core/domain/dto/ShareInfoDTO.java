package com.xiaobai1226.aether.core.domain.dto;

import com.xiaobai1226.aether.domain.entity.ShareDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分享信息实体类DTO
 *
 * @author bai
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ShareInfoDTO extends ShareDO {

    /**
     * 分享文件名称
     */
    private String name;

    /**
     * 分享时间
     */
    private String shareTime;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 是否是当前用户
     */
    private Boolean currentUser;
}