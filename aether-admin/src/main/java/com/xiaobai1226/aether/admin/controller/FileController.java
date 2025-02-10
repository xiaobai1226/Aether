package com.xiaobai1226.aether.admin.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.admin.domain.vo.FileVO;
import com.xiaobai1226.aether.admin.service.intf.FileService;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.FileDO;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 生成缩略图
     */
    @Post
    @Mapping("/generateThumbnails")
    public void generateThumbnails(@Param("fileIds") String fileIds) {
        List<Integer> ids = Arrays.stream(fileIds.split(StrUtil.COMMA)).mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

        // 获取要处理的文件
        var fileDOList = fileService.getNoThumbnailFileDOList(ids);

        if (CollUtil.isNotEmpty(fileDOList)) {
            fileService.generateThumbnails(fileDOList);
//            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_ID_EMPTY);
        }
    }
}
