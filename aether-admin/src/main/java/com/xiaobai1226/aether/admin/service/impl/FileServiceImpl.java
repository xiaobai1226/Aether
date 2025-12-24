package com.xiaobai1226.aether.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaobai1226.aether.admin.domain.vo.FileVO;
import com.xiaobai1226.aether.admin.service.intf.FileService;
import com.xiaobai1226.aether.common.constant.FolderNameConsts;
import com.xiaobai1226.aether.common.constant.SystemConsts;
import com.xiaobai1226.aether.common.enums.CategoryEnum;
import com.xiaobai1226.aether.common.enums.FileTypeEnum;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.common.util.ImageUtils;
import com.xiaobai1226.aether.common.util.VideoUtils;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.entity.FileDO;
import com.xiaobai1226.aether.dao.mapper.FileMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xiaobai1226.aether.common.enums.CategoryEnum.PICTURE;
import static com.xiaobai1226.aether.common.enums.CategoryEnum.VIDEO;

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
                var finalFullFilePath = FileUtils.generatePath(rootPath, fileDO.getPath());
                
                // 根据文件类型确定缩略图后缀
                var thumbnailSuffix = FileTypeEnum.isGif(FileNameUtil.extName(fileDO.getName()).toLowerCase())
                        ? SystemConsts.THUMBNAIL_GIF_SUFFIX
                        : SystemConsts.THUMBNAIL_SUFFIX;
                
                // 生成缩略图相对文件名（按日期分目录存储）
                String thumbnailFileName = DateUtil.format(new Date(), "yyyy/MM/dd") + StrUtil.SLASH
                        + FileUtils.replaceFileExtName(fileDO.getName(), thumbnailSuffix);
                
                // 生成缩略图完整路径
                var thumbnailFilePath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_THUMBNAIL_FILE_FULL,
                        thumbnailFileName);
                
                // 根据文件类型生成缩略图
                boolean result = false;
                if (CategoryEnum.isPictureBySuffix(fileDO.getSuffix())) {
                    result = ImageUtils.generateThumbnail(finalFullFilePath, thumbnailFilePath, 150, -1);
                } else if (CategoryEnum.isVideoBySuffix(fileDO.getSuffix())) {
                    result = VideoUtils.generateThumbnail(finalFullFilePath, thumbnailFilePath, 150);
                }
                
                if (result) {
                    updateFileThumbnail(fileDO.getId(), thumbnailFileName);
                }
            } catch (Exception e) {
                log.error("生成缩略图失败: fileId={}, fileName={}", fileDO.getId(), fileDO.getName(), e);
            }
        });
    }

    @Override
    public Boolean updateFileThumbnail(Long id, String thumbnail) {
        var lambdaUpdateWrapper = new LambdaUpdateWrapper<FileDO>();
        lambdaUpdateWrapper.set(FileDO::getThumbnail, thumbnail).eq(FileDO::getId, id);
        var updateNameCount = fileMapper.update(null, lambdaUpdateWrapper);

        return updateNameCount != 1;
    }
}
