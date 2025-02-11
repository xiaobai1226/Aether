package com.xiaobai1226.aether.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.solon.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.solon.plugins.pagination.Page;
import com.xiaobai1226.aether.admin.domain.vo.FileVO;
import com.xiaobai1226.aether.admin.service.intf.FileService;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.constant.SystemConsts;
import com.xiaobai1226.aether.common.enums.CategoryEnum;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.common.util.ImageUtils;
import com.xiaobai1226.aether.common.util.VideoUtils;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.FileDO;
import com.xiaobai1226.aether.domain.entity.UserFileDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xiaobai1226.aether.common.enums.CategoryEnum.PICTURE;
import static com.xiaobai1226.aether.common.enums.CategoryEnum.VIDEO;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;

/**
 * 文件service实现类
 *
 * @author bai
 */
@Component
@Slf4j
public class FileServiceImpl implements FileService {

    @Db
    private FileMapper fileMapper;

    @Inject("${project.path.root}")
    private String rootPath;

    @Override
    public PageResult<FileDO> getFileList(FileVO fileVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(fileMapper);
        var fileListPage = lambdaQuery.page(new Page<>(fileVO.getPageNum(), fileVO.getPageSize()));

        // 判断结果是否为空
        if (fileListPage == null || CollUtil.isEmpty(fileListPage.getRecords())) {
            return null;
        }

        return PageResult.with(fileListPage);
    }

    @Override
    public List<FileDO> getNoThumbnailFileDOList(List<Integer> ids) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(fileMapper);

        if (CollUtil.isNotEmpty(ids)) {
            lambdaQuery.in(FileDO::getId, ids);
        }

        var suffixSet = Stream.concat(PICTURE.suffixSet().stream(), VIDEO.suffixSet().stream()).collect(Collectors.toSet());
        lambdaQuery.in(FileDO::getSuffix, suffixSet);

        lambdaQuery.isNull(FileDO::getThumbnail);

        return lambdaQuery.list();
    }

//    @Override
//    public List<FileDO> getFileDOListByIds(List<Integer> ids) {
//        var lambdaQuery = new LambdaQueryChainWrapper<>(fileMapper);
//        return lambdaQuery.in(FileDO::getId, ids).list();
//    }

    @Override
    public void generateThumbnails(List<FileDO> fileDOList) {
        fileDOList.forEach(fileDO -> {
            try {
//                if (CategoryEnum.isPictureBySuffix(fileDO.getSuffix()) || CategoryEnum.isVideoBySuffix(fileDO.getSuffix())) {
                String thumbnailFileName = DateUtil.format(new Date(), "yyyy/MM/dd") + StrUtil.SLASH + FileUtils.replaceFileExtName(fileDO.getName(), SystemConsts.THUMBNAIL_SUFFIX);
                // 设置文件存储全路径
                var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL, thumbnailFileName);

                var finalFullFilePath = FileUtils.generatePath(rootPath, fileDO.getPath());

                // 图片生成缩略图
                if (CategoryEnum.isPictureBySuffix(fileDO.getSuffix())) {
                    var result = ImageUtils.generateThumbnail(finalFullFilePath, thumbnailFilePath, 150, -1);
                    thumbnailFileName = result ? thumbnailFileName : null;
                } else if (CategoryEnum.isVideoBySuffix(fileDO.getSuffix())) { // 视频生成缩略图
                    var result = VideoUtils.generateThumbnail(finalFullFilePath, thumbnailFilePath, 150);
                    thumbnailFileName = result ? thumbnailFileName : null;
                } else {
                    thumbnailFileName = null;
                }

                if (thumbnailFileName != null) {
                    updateFileThumbnail(fileDO.getId(), thumbnailFileName);
                }
//                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    @Override
    public Boolean updateFileThumbnail(Integer id, String thumbnail) {
        var lambdaUpdateWrapper = new LambdaUpdateWrapper<FileDO>();
        lambdaUpdateWrapper.set(FileDO::getThumbnail, thumbnail).eq(FileDO::getId, id);
        var updateNameCount = fileMapper.update(null, lambdaUpdateWrapper);

        return updateNameCount != 1;
    }
}
