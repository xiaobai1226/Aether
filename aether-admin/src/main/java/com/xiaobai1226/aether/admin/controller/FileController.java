package com.xiaobai1226.aether.admin.controller;

import com.xiaobai1226.aether.admin.domain.vo.FileVO;
import com.xiaobai1226.aether.admin.service.intf.FileService;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.FileDO;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.validation.annotation.Valid;

import static com.xiaobai1226.aether.common.constant.GateWayTagConsts.API_ADMIN;

/**
 * 文件Controller
 *
 * @author bai
 */
@Component(tag = API_ADMIN)
@Mapping("/file")
@Valid
public class FileController {

    @Inject
    private FileService fileService;

    /**
     * 分页获取文件列表
     *
     * @param fileVO 文件VO
     */
    @Get
    @Mapping("/getFileListByPage")
    public PageResult<FileDO> getFileListByPage(FileVO fileVO) {
        return fileService.getFileList(fileVO);
    }
}
