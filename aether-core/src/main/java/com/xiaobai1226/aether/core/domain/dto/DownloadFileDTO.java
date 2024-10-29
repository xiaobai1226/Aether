package com.xiaobai1226.aether.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 下载文件实体类DTO
 *
 * @author bai
 */
@Data
@AllArgsConstructor
public class DownloadFileDTO {

    /**
     * 要下载的文件ID集合
     */
    private List<Integer> ids;

    /**
     * 用户ID
     */
    private Integer userId;

}