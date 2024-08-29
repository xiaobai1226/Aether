package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 回收站文件实体类DTO
 *
 * @author bai
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RecycleBinFileDTO extends UserFileDTO {
    /**
     * 回收ID
     */
    private String recycleId;

    /**
     * 创建时间
     */
    private String deleteTime;
}