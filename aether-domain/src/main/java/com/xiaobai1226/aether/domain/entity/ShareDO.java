package com.xiaobai1226.aether.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 分享实体类DO
 *
 * @author bai
 */
@TableName("share")
@Accessors(chain = true)
@Data
public class ShareDO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 提取码，4位字符串，只包含字母数字
     */
    private String extractionCode;

    /**
     * 浏览次数
     */
    private Integer viewNum;

    /**
     * 下载次数
     */
    private Integer downloadNum;

    /**
     * 保存次数
     */
    private Integer saveNum;

    /**
     * 有效期，单位 天，0为永久
     */
    private Integer validityPeriod;

    /**
     * 所属用户ID
     */
    private Integer userId;

    /**
     * 创建时间
     */
    private String createTime;
}