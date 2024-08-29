package com.xiaobai1226.aether.core.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaobai1226.aether.core.dao.redis.ShareRedisDAO;
import com.xiaobai1226.aether.core.domain.dto.*;
import com.xiaobai1226.aether.core.domain.entity.UserFileDO;
import com.xiaobai1226.aether.core.domain.vo.share.*;
import com.xiaobai1226.aether.core.domain.vo.common.PageVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.enums.UserFileStatusEnum;
import com.xiaobai1226.aether.core.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.ShareFileService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.util.Result;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.xiaobai1226.aether.core.constant.GateWayTagConsts.API_V1;
import static com.xiaobai1226.aether.core.constant.result.error.ResultShareErrMsgConsts.*;
import static com.xiaobai1226.aether.core.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.core.enums.ResultSuccessMsgEnum.*;

/**
 * 分享文件Controller
 *
 * @author bai
 */
@Component(tag = API_V1)
@Mapping("/share")
@Valid
public class ShareFileController {

    @Inject
    private ShareFileService shareFileService;

    @Inject
    private UserFileService userFileService;

    /**
     * 分享文件Redis缓存
     */
    @Inject
    private ShareRedisDAO shareRedisDAO;

    /**
     * 创建分享文件
     */
    @Post
    @Mapping("/create")
    public Result<CreateShareFileDTO> create(@Validated CreateShareFileVO createShareFileVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        var ids = Arrays.stream(createShareFileVO.getIds().split(StrUtil.COMMA)).mapToInt(Integer::parseInt).boxed().toList();

        if (CollUtil.isEmpty(ids)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_SHARE_CONTENT_EMPTY);
        }

        var userFileDOList = userFileService.getUserFileByIdsAndUserId(ids, userId, UserFileStatusEnum.NORMAL);

        if (CollUtil.isEmpty(userFileDOList) || ids.size() != userFileDOList.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_SHARE_CONTENT_EMPTY);
        }

        var extractionCode = createShareFileVO.getExtractionCode();
        if (StrUtil.isEmpty(extractionCode)) {
            extractionCode = RandomUtil.randomString(4);
        }

        String shareId = shareFileService.create(ids, extractionCode, createShareFileVO.getValidityPeriod(), userId);

        return Result.success(SUCCESS_MSG_CREATE_SHARE.msg(), new CreateShareFileDTO(shareId, extractionCode, createShareFileVO.getValidityPeriod()));
    }

    /**
     * 分页获取分享列表
     *
     * @param shareFileVO 分享文件VO
     */
    @Get
    @Mapping("/getShareListByPage")
    public PageResultDataDTO<ShareFileDTO> getShareListByPage(PageVO shareFileVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        return shareFileService.getShareFileList(userId, shareFileVO);
    }

    /**
     * 取消分享
     */
    @Post
    @Mapping("/cancel")
    public Result<CreateShareFileDTO> cancel(@Body CancelShareFileVO cancelShareFileVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        var shareIds = Arrays.stream(cancelShareFileVO.getIds().split(StrUtil.COMMA)).filter(s -> !s.isEmpty()).toList();

        if (CollUtil.isEmpty(shareIds)) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_SHARE_CANCEL_CONTENT_EMPTY);
        }

        var shareFileDOList = shareFileService.getShareDOList(userId, shareIds);

        if (CollUtil.isEmpty(shareFileDOList) || shareFileDOList.size() != shareIds.size()) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_SHARE_CANCEL_CONTENT_NOT_EXIST);
        }

        shareFileService.cancelShareFile(userId, shareIds);

        return Result.success(SUCCESS_MSG_CANCEL_SHARE.msg());
    }

    /**
     * 获取分享信息
     *
     * @param shareId 分享ID
     */
    @Get
    @Mapping("/getShareInfo")
    public ShareInfoDTO getShareInfo(@Param("shareId") String shareId) {
        return shareFileService.getShareInfo(shareId);
    }




//    /**
//     * 获取分享信息 TODO 临时，流程需要重新设计
//     *
//     * @param shareId 分享ID
//     */
//    @Get
//    @Mapping("/getShareLoginInfo")
//    public ShareInfoDTO getShareLoginInfo(@Param("shareId") String shareId) {
//        Integer userId = null;
//        if (StpUtil.isLogin()) {
//            // 获取当前会话账号id, 并转化为`int`类型
//            userId = StpUtil.getLoginIdAsInt();
//        }
//
//        var shareInfo = shareRedisDAO.getShareInfo(shareId);
//        if (shareInfo == null) {
//            return null;
//        }
//
//        var shareInfoDTO = shareFileService.getShareInfo(shareId);
//
//        if (shareInfoDTO != null && Objects.equals(shareInfo.getUserId(), userId)) {
//            shareInfoDTO.setCurrentUser(true);
//        }
//
//        return shareInfoDTO;
//    }

//    /**
//     * 校验分享提取码
//     *
//     * @param checkExtractionCodeVO 检查校验码VO
//     */
//    @Post
//    @Mapping("/checkExtractionCode")
//    public void checkExtractionCode(@Body CheckExtractionCodeVO checkExtractionCodeVO) {
//        shareFileService.checkExtractionCode(checkExtractionCodeVO);
//    }
//
//    /**
//     * 分页获取分享文件内文件列表
//     *
//     * @param getShareFileInfoListVO 分享文件VO
//     */
//    @Get
//    @Mapping("/getShareFileListByShareIdPagination")
//    public PageResultDataDTO<UserFileDTO> getShareFileListByShareIdPagination(GetShareFileInfoListVO getShareFileInfoListVO) {
//
//        var shareInfo = shareRedisDAO.getShareInfo(getShareFileInfoListVO.getShareId());
//        if (shareInfo == null) {
//            // TODO 需要返回特定状态码，表示没有输入校验码校验
//            return null;
//        }
//
//        Integer parentId = null;
//        if (getShareFileInfoListVO.getPath() == null || getShareFileInfoListVO.getPath().isEmpty()) {
//            return shareFileService.getShareFileInfoListByShareId(getShareFileInfoListVO);
//        }
//
//        var path = getShareFileInfoListVO.getPath();
//        // 获取path的第一个元素
//        if (path.startsWith("/")) {
//            path = path.substring(1);
//        }
//
//        var dirs = path.split("/");
//        var rootPath = dirs[0];
//
//        if (rootPath == null || rootPath.isEmpty()) {
//            throw new FailResultException(PARAM_IS_INVALID, "路径错误");
//        }
//
//        var shareFileDTO = shareFileService.getShareFileDTOByShareIdAndName(getShareFileInfoListVO.getShareId(), rootPath);
//        if (shareFileDTO == null) {
//            throw new FailResultException(PARAM_IS_INVALID, "路径错误");
//        }
//
//        var userFileDO = userFileService.getParentFolderByPath(shareFileDTO.getUserId(), shareFileDTO.getParentId(), getShareFileInfoListVO.getPath());
//
//        if (userFileDO == null) {
//            throw new FailResultException(PARAM_IS_INVALID, "路径错误");
//        }
//
//        var userFileVO = new UserFileVO();
//        userFileVO.setPageNum(getShareFileInfoListVO.getPageNum());
//        userFileVO.setPageSize(getShareFileInfoListVO.getPageSize());
//        var finalPath = StrUtil.SLASH.equals(userFileDO.getPath()) ? userFileDO.getPath() + userFileDO.getName() : userFileDO.getPath() + StrUtil.SLASH + userFileDO.getName();
//        userFileVO.setPath(finalPath);
//
//        // TODO parentId随便写的0，要按时实际获取
//        return userFileService.getFileList(userFileDO.getUserId(), 0, userFileVO);
//    }

    /**
     * 保存到网盘
     *
     * @param save2NetdiskVO 保存到网盘信息
     */
    @Post
    @Mapping("/save2NetDisk")
    public void saveToNetDisk(Save2NetdiskVO save2NetdiskVO) {
        // 获取当前会话账号id, 并转化为`int`类型
        final var userId = StpUtil.getLoginIdAsInt();

        var shareInfo = shareRedisDAO.getShareInfo(save2NetdiskVO.getShareId());
        if (shareInfo == null) {
            // TODO 抛出异常，表示没有输入校验码校验
            return;
        }

        if (userId == shareInfo.getUserId()) {
            throw new FailResultException(PARAM_IS_INVALID, "不能分享给自己");
        }

        List<Integer> sourceIds = Arrays.stream(save2NetdiskVO.getIdsStr().split(",")).mapToInt(Integer::parseInt).boxed().toList();

        if (sourceIds.isEmpty()) {
            throw new FailResultException(PARAM_IS_INVALID, "分享内容不能为空");
        }

        var sourceUserFileTreeDTOList = userFileService.getUserFileTreeDTOByIdsAndUserId(sourceIds, shareInfo.getUserId(), UserFileStatusEnum.NORMAL.flag());

        if (sourceUserFileTreeDTOList == null || sourceUserFileTreeDTOList.size() != sourceIds.size()) {
            throw new FailResultException(PARAM_IS_INVALID, "分享内容不能为空");
        }

        // TODO 校验分享文件是否属于本次分享

        // 校验目标文件夹是否存在
        UserFileDO targetUserFileDO = null;
        var targetId = 0;
        // 如果不是根目录则判断目标文件夹是否存在
        if (save2NetdiskVO.getPath() != null) {
            targetUserFileDO = userFileService.getParentUserFileByPathAndItemType(userId, save2NetdiskVO.getPath());

            if (targetUserFileDO == null || !Objects.equals(UserFileItemTypeEnum.FOLDER.flag(), targetUserFileDO.getItemType())) {
                throw new FailResultException(PARAM_IS_INVALID, "目标文件夹不存在");
            }
            targetId = targetUserFileDO.getId();
        }

        // 获取全部要复制文件
        userFileService.recursiveGetUserFileTreeDTO(sourceUserFileTreeDTOList, userId);
        var totalSize = userFileService.getUserFileTreeSpaceUsage(sourceUserFileTreeDTOList);

        // 检测存储空间是否足够
//        var userSpaceUsage = userService.getUserSpaceUsage(userId);
//        if (userSpaceUsage == null || userSpaceUsage.getRealRemainStorage() < totalSize) {
//            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_INSUFFICIENT_STORAGE);
//        }

        // 复制文件
//        var result = userFileService.copy(targetUserFileDO, userId, sourceUserFileTreeDTOList, totalSize);

//        if (!result) {
//            throw new FailResultException(BAD_REQUEST_ERROR, "复制失败");
//        }

//        return ResultDataUtils.success(SUCCESS_MSG_COPY.msg());
    }
}
