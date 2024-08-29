package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.solon.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.solon.plugins.pagination.Page;
import com.baomidou.mybatisplus.solon.service.impl.ServiceImpl;
import com.xiaobai1226.aether.core.domain.dto.PageResultDataDTO;
import com.xiaobai1226.aether.core.domain.dto.RecycleBinFileDTO;
import com.xiaobai1226.aether.core.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.core.domain.entity.FileDO;
import com.xiaobai1226.aether.core.domain.entity.RecycleBinDO;
import com.xiaobai1226.aether.core.domain.entity.UserDO;
import com.xiaobai1226.aether.core.domain.entity.UserFileDO;
import com.xiaobai1226.aether.core.domain.vo.common.PageVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.exception.FailResultException;
import com.xiaobai1226.aether.core.mapper.FileMapper;
import com.xiaobai1226.aether.core.mapper.RecycleBinMapper;
import com.xiaobai1226.aether.core.mapper.UserFileMapper;
import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.core.util.FileUtils;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.*;

import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_DEL_CONTENT_EMPTY;
import static com.xiaobai1226.aether.core.constant.ResultErrorMsgConsts.ERROR_RESTORE_CONTENT_EMPTY;
import static com.xiaobai1226.aether.core.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.core.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.DEL;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 回收站服务接口实现类
 *
 * @author bai
 */
@Component
public class RecycleBinServiceImpl extends ServiceImpl<RecycleBinMapper, RecycleBinDO> implements RecycleBinService {

    @Db
    private UserFileMapper userFileMapper;

    @Db
    private FileMapper fileMapper;

    @Db
    private RecycleBinMapper recycleBinMapper;

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject
    private UserService userService;

    @Inject
    private UserFileService userFileService;

    @Tran
    @Override
    public Boolean insertBatch(List<RecycleBinDO> recycleBinList) {
        if (CollUtil.isEmpty(recycleBinList)) {
            return false;
        }
        return this.saveBatch(recycleBinList);
    }

    @Override
    public PageResultDataDTO<RecycleBinFileDTO> getRecycleBinList(Integer userId, PageVO recycleBinVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper);
        var recycleBinListPage = lambdaQuery.eq(RecycleBinDO::getUserId, userId).eq(RecycleBinDO::getRoot, 1).orderByDesc(RecycleBinDO::getCreateTime).page(new Page<>(recycleBinVO.getPageNum(), recycleBinVO.getPageSize()));

        // 判断结果是否为空
        if (recycleBinListPage == null || CollUtil.isEmpty(recycleBinListPage.getRecords())) {
            return null;
        }

        var userFileIds = new ArrayList<Integer>();
        for (var recycleBin : recycleBinListPage.getRecords()) {
            userFileIds.add(recycleBin.getUserFileId());
        }

        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(DEL.flag());
        var userFileList = userFileMapper.getUserFileDTOByIds(userFileDO, userFileIds);

        if (CollUtil.isEmpty(userFileList) || recycleBinListPage.getRecords().size() != userFileList.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var recycleBinDTOList = new ArrayList<RecycleBinFileDTO>();
        for (var recycleBinDO : recycleBinListPage.getRecords()) {
            for (UserFileDTO userFileDTO : userFileList) {
                if (userFileDTO.getId().equals(recycleBinDO.getUserFileId())) {
                    var recycleBinDTO = BeanUtil.toBean(userFileDTO, RecycleBinFileDTO.class);
                    recycleBinDTO.setRecycleId(recycleBinDO.getRecycleId());
                    recycleBinDTO.setDeleteTime(recycleBinDO.getCreateTime());

                    recycleBinDTOList.add(recycleBinDTO);

                    break;
                }
            }
        }

        var recycleBinFileIPage = new Page<RecycleBinFileDTO>(recycleBinListPage.getCurrent(), recycleBinListPage.getSize(), recycleBinListPage.getTotal());
        recycleBinFileIPage.setPages(recycleBinListPage.getPages());
        recycleBinFileIPage.setRecords(recycleBinDTOList);
        return new PageResultDataDTO<>(recycleBinFileIPage);
    }

    @Tran
    @Override
    public void delete(Integer userId, List<String> recycleIds) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper);
        var recycleBinWrapper = lambdaQuery.eq(RecycleBinDO::getUserId, userId);

        // 删除全部文件，如果只有一个元素，且为-1, 则为全部删除
        if (recycleIds.size() != 1 || !Objects.equals(recycleIds.get(0), "-1")) {
            recycleBinWrapper.in(RecycleBinDO::getRecycleId, recycleIds);
        }

        var recycleBinDOList = recycleBinWrapper.list();

        // 判断结果是否为空
        if (CollUtil.isEmpty(recycleBinDOList)) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_DEL_CONTENT_EMPTY);
        }

        // 回收站文件ID
        var recycleBinIds = new ArrayList<Integer>();
        // 用户文件ID
        var userFileIds = new ArrayList<Integer>();
        for (var recycleBinDO : recycleBinDOList) {
            userFileIds.add(recycleBinDO.getUserFileId());
            recycleBinIds.add(recycleBinDO.getId());
        }

        // 删除回收站内容
        var delRecycleBinFileCount = recycleBinMapper.deleteBatchIds(recycleBinIds);
        if (delRecycleBinFileCount != recycleBinIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(DEL.flag());
        var userFileList = userFileMapper.getUserFileDTOByIds(userFileDO, userFileIds);

        if (CollUtil.isEmpty(userFileList) || recycleBinDOList.size() != userFileList.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var fileIds = new HashSet<Integer>();
        var totalSize = 0L;
        for (var userFile : userFileList) {
            if (UserFileItemTypeEnum.isFile(userFile.getItemType()) && userFile.getFileId() != null) {
                fileIds.add(userFile.getFileId());
                totalSize += userFile.getSize();
            }
        }

        // 删除用户文件内容
        var delUserFileCount = userFileMapper.deleteBatchIds(userFileIds);
        if (delUserFileCount != userFileIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 更新用户所使用的空间
        if (totalSize > 0) {
            // 更新用户已使用存储空间
            var userSpaceUsageDTO = userService.getUserSpaceUsage(userId);
            var userDO = new UserDO();
            userDO.setId(userId);
            userDO.setUsedStorage(userSpaceUsageDTO.getUsedStorage() - totalSize);
            userService.updateUser(userDO);
        }

        if (CollUtil.isEmpty(fileIds)) {
            return;
        }

        // TODO 确认一个事务内，前面删除了，这里是否查得到，如果查得到会导致逻辑出错，file无法删除了
        var userFileLambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        var userFileDOList = userFileLambdaQuery.in(UserFileDO::getFileId, fileIds).list();


        var delFileIds = new HashSet<Integer>();
        // 如果为空，直接返回
        if (CollUtil.isEmpty(userFileDOList)) {
            delFileIds = fileIds;
        } else {
            for (var fileId : fileIds) {
                var isExist = false;
                for (var userFile : userFileDOList) {
                    if (Objects.equals(fileId, userFile.getFileId())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    delFileIds.add(fileId);
                }
            }
        }

        if (CollUtil.isEmpty(delFileIds)) {
            return;
        }

        // TODO 以下操作单独封装在FileService中，并可以异步执行
        // 查询要删除的文件详情
        var fileLambdaQuery = new LambdaQueryChainWrapper<>(fileMapper);
        var delFileList = fileLambdaQuery.in(FileDO::getId, delFileIds).list();
        if (CollUtil.isEmpty(delFileList)) {
            return;
        }

        // 删除文件内容
        var delFileCount = fileMapper.deleteBatchIds(delFileIds);
        if (delFileCount != delFileIds.size()) {
            // TODO 返回前台成功，增加后台报警机制
        }

        // 删除文件
        for (var delFile : delFileList) {
            FileUtils.deleteFile(delFile, rootPath);
        }
    }

    @Tran
    @Override
    public void restore(Integer userId, List<String> recycleIds) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper);
        var recycleBinDOList = lambdaQuery.eq(RecycleBinDO::getUserId, userId).in(RecycleBinDO::getRecycleId, recycleIds).list();

        // 判断结果是否为空
        if (CollUtil.isEmpty(recycleBinDOList)) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_RESTORE_CONTENT_EMPTY);
        }

        // 回收ID
        var recycleBinIds = new ArrayList<Integer>();
        // 用户文件ID
        var userFileIds = new ArrayList<Integer>();
        // 根文件ID
        var rootUserFileIdSet = new HashSet<Integer>();
        for (var recycleBinDO : recycleBinDOList) {
            userFileIds.add(recycleBinDO.getUserFileId());
            recycleBinIds.add(recycleBinDO.getId());
            if (recycleBinDO.getRoot() == 1) {
                rootUserFileIdSet.add(recycleBinDO.getUserFileId());
            }
        }

        // 删除回收站内容
        var delRecycleBinFileCount = recycleBinMapper.deleteBatchIds(recycleBinIds);
        if (delRecycleBinFileCount != recycleBinIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 查询用户文件信息
        var userFileLambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        var userFileDOList = userFileLambdaQuery.eq(UserFileDO::getUserId, userId).eq(UserFileDO::getFileStatus, DEL.flag()).in(UserFileDO::getId, userFileIds).list();

        if (CollUtil.isEmpty(userFileDOList) || recycleBinDOList.size() != userFileDOList.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        for (var userFileDO : userFileDOList) {
            if (!rootUserFileIdSet.contains(userFileDO.getId())) {
                continue;
            }

            // 检查父目录是否存在
            // 父文件存在标识
            var parentExist = true;
            // 如果不为根目录
            if (userFileDO.getParentId() != 0) {
                // 查询文件父节点是否存在
                var userFileParentDO = userFileService.getUserFileByIdAndUserId(userFileDO.getParentId(), userId, NORMAL);

                // 查询结果为空
                if (userFileParentDO == null) {
                    parentExist = false;
                }
            }

            var parentId = userFileDO.getParentId();

            // 如果父目录不存在
            if (!parentExist) {
                // 如果父目录不存在，则将parentId改为根节点
                parentId = 0;
            }

            // 检查父目录下，有没有与根元素同名文件或文件夹
//            var sameNameFile = userFileService.getUserFileByName(userFileDO.getName(), userId, parentId, NORMAL, UserFileItemTypeEnum.getEnumByFlag(userFileDO.getItemType()));
            var sameNameFile = userFileService.getUserFileByName(userFileDO.getName(), userId, parentId, NORMAL);

            // 查询结果不为空
            if (sameNameFile != null) {
                // 有同名文件\文件夹，修改文件名
                var newName = FileUtils.rename(userFileDO.getName());
                var updateResult = userFileService.updateFileNameById(userFileDO.getId(), userId, newName, DEL);

                if (!updateResult) {
                    throw new FailResultException(SYSTEM_ERROR);
                }
            }

            // 如果父目录不存在
            if (!parentExist) {
                // 如果父目录不存在，则将父目录改为根节点
                userFileService.updateParentIdByIds(List.of(userFileDO.getId()), 0, userId, DEL);
            }
        }

        // 修改状态
        userFileService.updateUserFileStatusById(userFileIds, userId, NORMAL);
    }
}