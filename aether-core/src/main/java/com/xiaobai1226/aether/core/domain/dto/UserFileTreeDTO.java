package com.xiaobai1226.aether.core.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户文件实体类DTO
 *
 * @author bai
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserFileTreeDTO extends UserFileDTO {
    /**
     * 缩略图
     */
    private List<UserFileTreeDTO> childUserFileDTOList;
}