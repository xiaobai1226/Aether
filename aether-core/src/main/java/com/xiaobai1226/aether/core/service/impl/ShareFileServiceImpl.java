package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.solon.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.solon.plugins.pagination.Page;
import com.baomidou.mybatisplus.solon.service.impl.ServiceImpl;
import com.xiaobai1226.aether.core.dao.redis.ShareRedisDAO;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.core.domain.dto.ShareFileDTO;
import com.xiaobai1226.aether.core.domain.dto.ShareInfoDTO;
import com.xiaobai1226.aether.domain.entity.ShareDO;
import com.xiaobai1226.aether.domain.entity.ShareUserFileDO;
import com.xiaobai1226.aether.core.domain.vo.common.PageVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.mapper.ShareMapper;
import com.xiaobai1226.aether.core.mapper.ShareUserFileMapper;
import com.xiaobai1226.aether.core.mapper.UserFileMapper;
import com.xiaobai1226.aether.dao.mapper.UserMapper;
import com.xiaobai1226.aether.core.service.intf.ShareFileService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 分享服务接口实现类
 *
 * @author bai
 */
@Component
public class ShareFileServiceImpl extends ServiceImpl<ShareUserFileMapper, ShareUserFileDO> implements ShareFileService {

    @Db
    private UserFileMapper userFileMapper;

    @Db
    private ShareMapper shareMapper;

    @Db
    private ShareUserFileMapper shareUserFileMapper;

    @Db
    private UserMapper userMapper;

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private UserFileService userFileService;

    /**
     * 分享文件Redis缓存
     */
    @Inject
    private ShareRedisDAO shareRedisDAO;

    @Override
    @Tran
    public String create(List<Integer> userFileIds, String extractionCode, Integer validityPeriod, Integer userId) {
        var shareId = IdUtil.simpleUUID();

        var insertResult = shareMapper.insert(new ShareDO().setId(shareId).setExtractionCode(extractionCode).setValidityPeriod(validityPeriod).setUserId(userId));

        if (insertResult != 1) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var shareUserFileDOList = new ArrayList<ShareUserFileDO>();
        for (var id : userFileIds) {
            var shareUserFileDO = new ShareUserFileDO();
            shareUserFileDO.setShareId(shareId);
            shareUserFileDO.setUserFileId(id);
            shareUserFileDOList.add(shareUserFileDO);
        }

        var result = this.saveBatch(shareUserFileDOList);

        if (!result) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        return shareId;
    }

    @Override
    public Page<ShareDO> getShareDOListByPage(Integer userId, PageVO shareFileVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(shareMapper).eq(ShareDO::getUserId, userId).orderByDesc(ShareDO::getCreateTime);

        Page<ShareDO> page = new Page<>(shareFileVO.getPageNum(), shareFileVO.getPageSize());

        return lambdaQuery.page(page);
    }

    @Override
    public List<ShareDO> getShareDOList(Integer userId, List<String> shareIds) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(shareMapper).eq(ShareDO::getUserId, userId).in(ShareDO::getId, shareIds);

        return lambdaQuery.list();
    }

    @Override
    public PageResult<ShareFileDTO> getShareFileList(Integer userId, PageVO shareFileVO) {
        var shareDOListPage = getShareDOListByPage(userId, shareFileVO);

        if (shareDOListPage == null || CollUtil.isEmpty(shareDOListPage.getRecords())) {
            return null;
        }

        var shareIds = new ArrayList<String>();
        for (var shareDO : shareDOListPage.getRecords()) {
            shareIds.add(shareDO.getId());
        }

        var shareUserFileDOList = new LambdaQueryChainWrapper<>(shareUserFileMapper).in(ShareUserFileDO::getShareId, shareIds).list();

        if (CollUtil.isEmpty(shareUserFileDOList)) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var userFileIds = new ArrayList<Integer>();

        for (var shareUserFileDO : shareUserFileDOList) {
            userFileIds.add(shareUserFileDO.getUserFileId());
        }

        var userFileDTOList = userFileService.getUserFileDTOListByIds(userFileIds, userId, NORMAL);

        // 最终分享文件集合
        var finalShareFileDTOList = new ArrayList<ShareFileDTO>();
        ShareFileDTO shareFileDTO;
        int count;
        for (var shareDO : shareDOListPage.getRecords()) {
            count = 0;
            shareFileDTO = new ShareFileDTO();
            BeanUtil.copyProperties(shareDO, shareFileDTO);

            if (CollUtil.isNotEmpty(userFileDTOList)) {
                for (var shareUserFileDO : shareUserFileDOList) {
                    if (!shareDO.getId().equals(shareUserFileDO.getShareId())) {
                        continue;
                    }

                    for (var userFileDTO : userFileDTOList) {
                        if (!shareUserFileDO.getUserFileId().equals(userFileDTO.getId())) {
                            continue;
                        }

                        if (count == 0) {
                            shareFileDTO.setName(userFileDTO.getName());
                            shareFileDTO.setParentId(userFileDTO.getParentId());
                            shareFileDTO.setSize(userFileDTO.getSize());
                            shareFileDTO.setFileStatus(userFileDTO.getFileStatus());
                            shareFileDTO.setItemType(userFileDTO.getItemType());
                            shareFileDTO.setThumbnail(userFileDTO.getThumbnail());
                            shareFileDTO.setFileType(userFileDTO.getFileType());
//                            shareFileDTO.setCategory(userFileDTO.getCategory());
                        }

                        count++;
                        break;
                    }

                    if (count > 1) {
                        break;
                    }
                }
            }

            if (count == 0) {
                shareFileDTO.setName("分享的文件已被删除");
                shareFileDTO.setItemType(UserFileItemTypeEnum.FILE.flag());
//                shareFileDTO.setFileType(FileTypeEnum.OTHER.id());
            } else if (count > 1) {
                shareFileDTO.setName(shareFileDTO.getName() + " 等");
            }

            finalShareFileDTOList.add(shareFileDTO);
        }

        IPage<ShareFileDTO> shareFileDTOIPage = new Page<>(shareDOListPage.getCurrent(), shareDOListPage.getSize(), shareDOListPage.getTotal());
        shareFileDTOIPage.setPages(shareDOListPage.getPages());
        shareFileDTOIPage.setRecords(finalShareFileDTOList);
        return new PageResult<>(shareFileDTOIPage);
    }

    @Override
    @Tran
    public void cancelShareFile(Integer userId, List<String> shareIds) {
        var delCount = shareMapper.deleteBatchIds(shareIds);

        if (delCount != shareIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        LambdaUpdateWrapper<ShareUserFileDO> lambdaUpdate = Wrappers.lambdaUpdate();
        lambdaUpdate.in(ShareUserFileDO::getShareId, shareIds);
        shareUserFileMapper.delete(lambdaUpdate);
    }

    @Override
    public ShareInfoDTO getShareInfo(String shareId) {
//        var shareFileDTOList = shareFileMapper.getShareFileDTOListByShareId(null, List.of(shareId));
//
//        // 如果分享文件不存在，直接返回
//        if (shareFileDTOList == null || shareFileDTOList.isEmpty()) {
//            return null;
//        }
//
//        // 判断分享是否在有效期内
//        var validityPeriodResult = calculateValidityPeriod(shareFileDTOList.getFirst().getCreateTime(), shareFileDTOList.getFirst().getValidityPeriod());
//        if (!validityPeriodResult) {
//            return null;
//        }
//
//        ShareFileDTO finalShareFileDTO = null;
//        for (var shareFileDTO : shareFileDTOList) {
//            if (Objects.equals(NORMAL.flag(), shareFileDTO.getFileStatus())) {
//                if (finalShareFileDTO == null) {
//                    finalShareFileDTO = shareFileDTO;
//                } else {
//                    finalShareFileDTO.setName(finalShareFileDTO.getName() + " 等");
//                    break;
//                }
//            }
//        }
//
//        if (finalShareFileDTO == null) {
//            return null;
//        }
//
//        var userDO = userMapper.selectById(finalShareFileDTO.getUserId());
//
//        if (userDO == null || userDO.getUserStatus() != 1) {
//            return null;
//        }
//
//        // TODO 获取头像代码，后面要复用
//        final var avatarFolderPath = FileUtils.generatePath(rootPath, FolderNameConsts.PATH_AVATAR_FILE_FULL);
//
//        // 如果文件夹不存在则创建文件夹
//        if (!FileUtil.exists(Path.of(avatarFolderPath), false)) {
//            FileUtil.mkdir(avatarFolderPath);
//        }
//
//        // 设置头像存储全路径
//        var avatarPath = FileUtils.generatePath(avatarFolderPath, userDO.getId() + SystemConsts.AVATAR_SUFFIX);
//        String avatarBase64 = "data:image/jpg;base64,";
//        // 判断文件是否存在
//        if (FileUtil.exist(avatarPath)) {
//            var fileBytes = FileUtil.readBytes(avatarPath);
//            avatarBase64 = avatarBase64 + Base64.encode(fileBytes);
//        } else {
//            var defaultAvatarPath = FileUtils.generatePath("/avatar", SystemConsts.DEFAULT_AVATAR_FILE_NAME);
//            var resource = new ClassPathResource(defaultAvatarPath); // your classpath file
//            avatarBase64 = avatarBase64 + Base64.encode(IoUtil.readBytes(resource.getStream()));
//        }
//
//        var shareInfoDTO = new ShareInfoDTO();
//        shareInfoDTO.setName(finalShareFileDTO.getName());
//        shareInfoDTO.setShareTime(finalShareFileDTO.getCreateTime());
//        shareInfoDTO.setNickname(userDO.getNickname());
//        shareInfoDTO.setAvatar(avatarBase64);
//
//        return shareInfoDTO;
        return null;
    }


    // TODO 分页总数不对，查询了所有文件，没排除已删的文件，后面逻辑删除的，所以总数不对
//    @Override
//    public PageResultDataDTO<UserFileDTO> getShareFileInfoListByShareId(GetShareFileInfoListVO getShareFileInfoListVO) {
//        var shareFileDTOPage = shareFileMapper.getShareFileDTOListByShareIdAndPage(new Page<>(getShareFileInfoListVO.getPageNum(), getShareFileInfoListVO.getPageSize()), null, List.of(getShareFileInfoListVO.getShareId()));
//
//        if (shareFileDTOPage == null || shareFileDTOPage.getRecords().isEmpty()) {
//            return null;
//        }
//
////        var finalShareFileDTOList = new ArrayList<ShareFileDTO>();
//        var userFileIds = new ArrayList<Integer>();
//        ShareFileDTO finalShareFileDTO;
//        for (var shareFileDTO : shareFileDTOPage.getRecords()) {
//            if (Objects.equals(NORMAL.flag(), shareFileDTO.getFileStatus())) {
//                userFileIds.add(shareFileDTO.getUserFileId());
////                finalShareFileDTO = new ShareFileDTO();
////                BeanUtil.copyProperties(shareFileDTO, finalShareFileDTO);
//
////                finalShareFileDTOList.add(finalShareFileDTO);
////
////                if (Objects.equals(UserFileItemTypeEnum.FILE.flag(), shareFileDTO.getItemType())) {
////                    userFileIds.add(shareFileDTO.getUserFileId());
////                }
//            }
//        }
//
////        var finalUserFileDTOList = new ArrayList<UserFileDTO>();
//        if (userFileIds.isEmpty()) {
//            return null;
//        }
//
//        var userFileDO = new UserFileDO();
//        userFileDO.setFileStatus(NORMAL.flag());
//        userFileDO.setUserId(shareFileDTOPage.getRecords().getFirst().getUserId());
//        var userFileDTOList = userFileMapper.getUserFileDTOByIds(userFileDO, userFileIds);
//
//        if (userFileDTOList == null || userFileDTOList.isEmpty()) {
//            return null;
//        }
////        if (userFileDOList != null && !userFileDOList.isEmpty()) {
////            for (var shareFileDTO : finalShareFileDTOList) {
////                if (Objects.equals(UserFileItemTypeEnum.FILE.flag(), shareFileDTO.getItemType())) {
////                    for (var userFileDO1 : userFileDOList) {
////                        if (Objects.equals(shareFileDTO.getUserFileId(), userFileDO1.getId())) {
////                            shareFileDTO.setThumbnail(userFileDO1.getThumbnail());
////                            shareFileDTO.setFileType(userFileDO1.getFileType());
////                            shareFileDTO.setCategory(userFileDO1.getCategory());
////                            shareFileDTO.setSize(userFileDO1.getSize());
////
////                            break;
////                        }
////                    }
////                }
////            }
////        }
//
//        IPage<UserFileDTO> userFileDTOIPage = new Page<>(shareFileDTOPage.getCurrent(), shareFileDTOPage.getSize(), shareFileDTOPage.getTotal());
//        userFileDTOIPage.setPages(shareFileDTOPage.getPages());
//        userFileDTOIPage.setRecords(userFileDTOList);
//        return new PageResultDataDTO<>(userFileDTOIPage);
//    }
//
//    @Override
//    public List<ShareDO> getShareFileDOListByShareId(Integer userId, List<String> shareIds) {
//        var lambdaQuery = new LambdaQueryChainWrapper<>(shareFileMapper);
//        var shareFileDOList = lambdaQuery.eq(ShareDO::getUserId, userId).in(ShareDO::getShareId, shareIds).list();
//
//        if (shareFileDOList == null || shareFileDOList.isEmpty()) {
//            return null;
//        }
//
//        return shareFileDOList;
//    }

//    @Override
//    public void checkExtractionCode(CheckExtractionCodeVO checkExtractionCodeVO) {
//        var shareFileDTOList = shareFileMapper.getShareFileDTOListByShareId(null, List.of(checkExtractionCodeVO.getShareId()));
//
//        // 如果分享文件不存在，直接返回
//        if (shareFileDTOList == null || shareFileDTOList.isEmpty()) {
//            throw new FailResultException(PARAM_IS_INVALID, "分享文件不存在");
//        }
//
//        // 判断分享是否在有效期内
//        var validityPeriodResult = calculateValidityPeriod(shareFileDTOList.getFirst().getCreateTime(), shareFileDTOList.getFirst().getValidityPeriod());
//        if (!validityPeriodResult) {
//            throw new FailResultException(PARAM_IS_INVALID, "分享文件不存在");
//        }
//
//        ShareFileDTO finalShareFileDTO = null;
//        for (var shareFileDTO : shareFileDTOList) {
//            if (Objects.equals(NORMAL.flag(), shareFileDTO.getFileStatus())) {
//                if (finalShareFileDTO == null) {
//                    finalShareFileDTO = shareFileDTO;
//                } else {
//                    finalShareFileDTO.setName(finalShareFileDTO.getName() + " 等");
//                    break;
//                }
//            }
//        }
//
//        if (finalShareFileDTO == null) {
//            throw new FailResultException(PARAM_IS_INVALID, "分享文件不存在");
//        }
//
//        var userDO = userMapper.selectById(finalShareFileDTO.getUserId());
//
//        if (userDO == null || userDO.getUserStatus() != 1) {
//            throw new FailResultException(PARAM_IS_INVALID, "分享文件不存在");
//        }
//
//        var checkResult = Objects.equals(finalShareFileDTO.getExtractionCode(), checkExtractionCodeVO.getExtractionCode());
//
//        if (!checkResult) {
//            throw new FailResultException(PARAM_IS_INVALID, "提取码错误");
//        }
//
//        // TODO 将数据记录下来，短时间内打开不需要再次输入提取码，增加iP或设备信息，不然所有人打开这个都可以使用了
//        shareRedisDAO.putShareInfo(checkExtractionCodeVO.getShareId(), finalShareFileDTO);
//    }
//
//    @Override
//    public ShareFileDTO getShareFileDTOByShareIdAndName(String shareId, String name) {
//        var shareFileDTOList = shareFileMapper.getShareFileDTOListByShareId(null, List.of(shareId));
//
//        // 如果分享文件不存在，直接返回
//        if (shareFileDTOList == null || shareFileDTOList.isEmpty()) {
//            return null;
//        }
//
//        for (var shareFileDTO : shareFileDTOList) {
//            if (Objects.equals(NORMAL.flag(), shareFileDTO.getFileStatus()) && Objects.equals(UserFileItemTypeEnum.FOLDER.flag(), shareFileDTO.getItemType()) && Objects.equals(shareFileDTO.getName(), name)) {
//                return shareFileDTO;
//            }
//        }
//
//        return null;
//    }

    private Boolean calculateValidityPeriod(String shareTime, Integer validityPeriod) {
        if (validityPeriod == 0) {
            return true;
        }

        // 字符串转日期
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime shareTimeDate = LocalDateTime.parse(shareTime, format);

        // 计算有效期结束时间
        shareTimeDate = shareTimeDate.plusDays(validityPeriod);

        // 获取当前时间
        LocalDateTime currentTimeDate = LocalDateTime.now();

        // 剩余时间
        Duration duration = Duration.between(currentTimeDate, shareTimeDate);
        long remainingDays = duration.toDays();

        return remainingDays > 0;
    }
}