package com.xiaobai1226.aether.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.xiaobai1226.aether.core.domain.dto.RecycleBinFileDTO;
import com.xiaobai1226.aether.core.domain.vo.common.PageVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.QuotaService;
import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
import com.xiaobai1226.aether.core.service.intf.StorageSourceService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.intf.UserService;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.dao.domain.entity.RecycleBinDO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import com.xiaobai1226.aether.dao.mapper.FileMapper;
import com.xiaobai1226.aether.dao.mapper.RecycleBinMapper;
import com.xiaobai1226.aether.dao.mapper.UserFileMapper;
import com.xiaobai1226.aether.common.util.FileUtils;
import com.xiaobai1226.aether.core.service.support.FilePurgeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;

import java.util.*;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_DEL_CONTENT_EMPTY;
import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.ERROR_RESTORE_CONTENT_EMPTY;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.BAD_REQUEST_ERROR;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.DEL;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 回收站服务接口实现类
 *
 * @author bai
 */
@Component
@Slf4j
public class RecycleBinServiceImpl extends ServiceImpl<RecycleBinMapper, RecycleBinDO> implements RecycleBinService {

    @Db
    private UserFileMapper userFileMapper;

    @Db
    private FileMapper fileMapper;

    @Db
    private RecycleBinMapper recycleBinMapper;

    @Inject("${project.path.root}")
    private String rootPath;

    @Inject("${project.recycle-bin.retention-days:10}")
    private Integer retentionDays;

    @Inject
    private UserService userService;

    @Inject
    private QuotaService quotaService;

    @Inject
    private UserFileService userFileService;

    @Inject
    private StorageSourceService storageSourceService;

    @Inject
    private FilePurgeService filePurgeService;

    @Inject
    private StorageMigrationService storageMigrationService;

    @Tran
    @Override
    public Boolean insertBatch(List<RecycleBinDO> recycleBinList) {
        if (CollUtil.isEmpty(recycleBinList)) {
            return false;
        }
        return this.saveBatch(recycleBinList);
    }

    @Override
    public PageResult<RecycleBinFileDTO> getRecycleBinList(final Long userId, PageVO recycleBinVO) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper).eq(RecycleBinDO::getUserId, userId)
                .eq(RecycleBinDO::getRoot, 1);

        // 1 文件名 2 删除时间 3 文件大小 4 有效时间
        // if (recycleBinVO.getSortField() == 1 && recycleBinVO.getSortOrder() == 1) {
        // lambdaQuery.orderByDesc(RecycleBinDO::get);
        // } else if (recycleBinVO.getSortField() == 1 && recycleBinVO.getSortOrder() ==
        // 2) {
        // lambdaQuery.orderByDesc(RecycleBinDO::get);
        // } else

        if (recycleBinVO.getSortField() == 2 && recycleBinVO.getSortOrder() == 1) {
            lambdaQuery.orderByAsc(RecycleBinDO::getCreateTime);
        } else if (recycleBinVO.getSortField() == 2 && recycleBinVO.getSortOrder() == 2) {
            lambdaQuery.orderByDesc(RecycleBinDO::getCreateTime);
        }
        // else if (recycleBinVO.getSortField() == 3 && recycleBinVO.getSortOrder() ==
        // 1) {
        // lambdaQuery.orderByDesc(RecycleBinDO::getCreateTime);
        // } else if (recycleBinVO.getSortField() == 3 && recycleBinVO.getSortOrder() ==
        // 2) {
        // lambdaQuery.orderByDesc(RecycleBinDO::getCreateTime);
        // } else if (recycleBinVO.getSortField() == 4 && recycleBinVO.getSortOrder() ==
        // 1) {
        // lambdaQuery.orderByDesc(RecycleBinDO::getCreateTime);
        // } else if (recycleBinVO.getSortField() == 4 && recycleBinVO.getSortOrder() ==
        // 2) {
        // lambdaQuery.orderByDesc(RecycleBinDO::get);
        // }
        var recycleBinListPage = lambdaQuery.page(new Page<>(recycleBinVO.getPageNum(), recycleBinVO.getPageSize()));

        // 判断结果是否为空
        if (recycleBinListPage == null || CollUtil.isEmpty(recycleBinListPage.getRecords())) {
            return null;
        }

        var userFileIds = new ArrayList<Long>();
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

        var recycleBinFileIPage = new Page<RecycleBinFileDTO>(recycleBinListPage.getCurrent(),
                recycleBinListPage.getSize(), recycleBinListPage.getTotal());
        recycleBinFileIPage.setPages(recycleBinListPage.getPages());
        recycleBinFileIPage.setRecords(recycleBinDTOList);
        return new PageResult<>(recycleBinFileIPage);
    }

    @Tran
    @Override
    public void delete(final Long userId, List<String> recycleIds) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper);
        var recycleBinWrapper = lambdaQuery.eq(RecycleBinDO::getUserId, userId);

        // 删除全部文件，如果只有一个元素，且为-1, 则为全部删除
        if (recycleIds.size() != 1 || !Objects.equals(recycleIds.getFirst(), "-1")) {
            recycleBinWrapper.in(RecycleBinDO::getRecycleId, recycleIds);
        }

        var recycleBinDOList = recycleBinWrapper.list();

        // 判断结果是否为空
        if (CollUtil.isEmpty(recycleBinDOList)) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_DEL_CONTENT_EMPTY);
        }

        // 回收站文件ID
        var recycleBinIds = new ArrayList<Long>();
        // 用户文件ID
        var userFileIds = new ArrayList<Long>();
        for (var recycleBinDO : recycleBinDOList) {
            userFileIds.add(recycleBinDO.getUserFileId());
            recycleBinIds.add(recycleBinDO.getId());
        }

        // 删除回收站内容
        var delRecycleBinFileCount = recycleBinMapper.deleteByIds(recycleBinIds);
        if (delRecycleBinFileCount != recycleBinIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var userFileDO = new UserFileDO().setUserId(userId).setFileStatus(DEL.flag());
        var userFileList = userFileMapper.getUserFileDTOByIds(userFileDO, userFileIds);

        if (CollUtil.isEmpty(userFileList) || recycleBinDOList.size() != userFileList.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var fileIds = new HashSet<Long>();
        var totalSize = 0L;
        for (var userFile : userFileList) {
            if (UserFileItemTypeEnum.isFile(userFile.getItemType()) && userFile.getFileId() != null) {
                fileIds.add(userFile.getFileId());
                totalSize += userFile.getSize();
            }
        }

        // 删除用户文件内容
        var delUserFileCount = userFileMapper.deleteByIds(userFileIds);
        if (delUserFileCount != userFileIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 释放用户配额（QuotaService 内部已处理 bytes <= 0 的情况）
        quotaService.decreaseUsed(userId, totalSize);

        // 注意：物理文件（FileDO + 物理对象）的清理由 FileCleanupService 定时任务统一处理
        log.info("回收站文件已删除: userId={}, 删除{}个UserFile, 释放空间{}字节",
                userId, userFileIds.size(), totalSize);
    }

    @Tran
    @Override
    public void restore(final Long userId, List<String> recycleIds) {
        var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper);
        var recycleBinDOList = lambdaQuery.eq(RecycleBinDO::getUserId, userId)
                .in(RecycleBinDO::getRecycleId, recycleIds).list();

        // 判断结果是否为空
        if (CollUtil.isEmpty(recycleBinDOList)) {
            throw new FailResultException(BAD_REQUEST_ERROR, ERROR_RESTORE_CONTENT_EMPTY);
        }

        // 回收ID
        var recycleBinIds = new ArrayList<Long>();
        // 用户文件ID
        var userFileIds = new ArrayList<Long>();
        // 根文件ID
        var rootUserFileIdSet = new HashSet<Long>();
        for (var recycleBinDO : recycleBinDOList) {
            userFileIds.add(recycleBinDO.getUserFileId());
            recycleBinIds.add(recycleBinDO.getId());
            if (recycleBinDO.getRoot() == 1) {
                rootUserFileIdSet.add(recycleBinDO.getUserFileId());
            }
        }

        // 删除回收站内容
        var delRecycleBinFileCount = recycleBinMapper.deleteByIds(recycleBinIds);
        if (delRecycleBinFileCount != recycleBinIds.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 查询用户文件信息
        var userFileLambdaQuery = new LambdaQueryChainWrapper<>(userFileMapper);
        var userFileDOList = userFileLambdaQuery.eq(UserFileDO::getUserId, userId)
                .eq(UserFileDO::getFileStatus, DEL.flag()).in(UserFileDO::getId, userFileIds).list();

        if (CollUtil.isEmpty(userFileDOList) || recycleBinDOList.size() != userFileDOList.size()) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        // 存储本次还原文件的名称
        var restoreNameSet = new HashSet<String>();

        for (var userFileDO : userFileDOList) {
            if (!rootUserFileIdSet.contains(userFileDO.getId())) {
                continue;
            }

            // 检查父目录是否存在
            // 父文件存在标识
            var parentExist = true;
            UserFileDO userFileParentDO = null;
            // 如果不为根目录
            if (userFileDO.getParentId() != 0) {
                // 查询文件父节点是否存在
                userFileParentDO = userFileService.getUserFileByIdAndUserId(userFileDO.getParentId(), userId, NORMAL);

                // 查询结果为空
                if (userFileParentDO == null) {
                    parentExist = false;
                }
            }

            var parentId = userFileDO.getParentId();

            // 如果父目录不存在
            if (!parentExist) {
                // 如果父目录不存在，则将parentId改为根节点
                parentId = 0L;
            }

            // 是否重名
            var isExistRepeatName = false;

            // 检查已改名的文件，有没有同名文件或文件夹（避免还原多个同名文件或文件夹时导致还原后名称重复）
            var checkName = parentId + "-" + userFileDO.getName();
            if (restoreNameSet.contains(checkName)) {
                isExistRepeatName = true;
            } else {
                // 检查父目录下，有没有与根元素同名文件或文件夹
                // var sameNameFile = userFileService.getUserFileByName(userFileDO.getName(),
                // userId, parentId, NORMAL,
                // UserFileItemTypeEnum.getEnumByFlag(userFileDO.getItemType()));
                var sameNameFile = userFileService.getUserFileByName(userFileDO.getName(), userId, parentId, NORMAL);

                // 查询结果不为空
                if (sameNameFile != null) {
                    isExistRepeatName = true;
                }
            }

            // 如果重名则改名
            if (isExistRepeatName) {
                // 有同名文件\文件夹，修改文件名
                var newName = FileUtils.rename(userFileDO.getName());
                var updateResult = userFileService.updateFileNameById(userFileDO.getId(), userId, newName, DEL);

                if (!updateResult) {
                    throw new FailResultException(SYSTEM_ERROR);
                }

                restoreNameSet.add(parentId + "-" + newName);
            } else {
                restoreNameSet.add(checkName);
            }

            // 如果父目录不存在
            if (!parentExist) {
                // 如果父目录不存在，则将父目录改为根节点
                userFileService.updateParentIdByIds(List.of(userFileDO.getId()), 0L, userId, DEL);
            }
        }

        // 修改状态
        userFileService.updateUserFileStatusById(userFileIds, userId, NORMAL);

        // 处理存储源迁移（只对继承类型的文件/文件夹进行处理）
        for (var userFileDO : userFileDOList) {
            if (!rootUserFileIdSet.contains(userFileDO.getId())) {
                continue;
            }

            // 只处理继承类型的文件/文件夹（storage_source_type = 1）
            if (userFileDO.getStorageSourceType() == null || userFileDO.getStorageSourceType() != 1) {
                continue;
            }

            // 获取目标父目录的存储源ID
            Long targetStorageSourceId;
            if (userFileDO.getParentId() == 0) {
                // 父目录是根目录，获取默认存储源
                var defaultStorageSource = storageSourceService.getDefaultStorageSource(userId);
                if (defaultStorageSource == null) {
                    continue;
                }
                targetStorageSourceId = defaultStorageSource.getId();
            } else {
                // 获取父目录的存储源
                var parentUserFile = userFileService.getUserFileByIdAndUserId(userFileDO.getParentId(), userId, NORMAL);
                if (parentUserFile == null || parentUserFile.getStorageSourceId() == null) {
                    continue;
                }
                targetStorageSourceId = parentUserFile.getStorageSourceId();
            }

            // 如果存储源不一致，标记为待迁移（异步处理）
            if (!Objects.equals(userFileDO.getStorageSourceId(), targetStorageSourceId)) {
                // 标记为待迁移
                storageMigrationService.markForMigration(userFileDO.getId(), targetStorageSourceId, userId);
                log.info("回收站还原文件需要迁移存储源: userFileId={}, targetStorageId={}",
                        userFileDO.getId(), targetStorageSourceId);
            }
        }
    }

    @Override
    @Tran
    public void cleanExpiredRecycleBinFiles() {
        try {
            // 计算N天前的时间（从配置中读取保留天数）
            Date expireDate = DateUtil.offsetDay(new Date(), -retentionDays);
            String expireDateStr = DateUtil.format(expireDate, "yyyy-MM-dd HH:mm:ss");

            log.info("开始清理{}之前的回收站文件（保留期限{}天）", expireDateStr, retentionDays);

            // 查询10天前的回收站记录
            var lambdaQuery = new LambdaQueryChainWrapper<>(recycleBinMapper);
            var expiredRecycleBinList = lambdaQuery.lt(RecycleBinDO::getCreateTime, expireDateStr).list();

            if (CollUtil.isEmpty(expiredRecycleBinList)) {
                log.info("没有需要清理的过期回收站文件");
                return;
            }

            log.info("找到{}条过期回收站记录", expiredRecycleBinList.size());

            // 按用户分组处理
            Map<Long, List<String>> userRecycleIdMap = new HashMap<>();
            for (var recycleBinDO : expiredRecycleBinList) {
                userRecycleIdMap.computeIfAbsent(recycleBinDO.getUserId(), k -> new ArrayList<>())
                        .add(recycleBinDO.getRecycleId());
            }

            // 逐个用户清理
            int totalCleaned = 0;
            for (Map.Entry<Long, List<String>> entry : userRecycleIdMap.entrySet()) {
                Long userId = entry.getKey();
                List<String> recycleIds = entry.getValue();

                try {
                    // 调用删除方法
                    delete(userId, recycleIds);
                    totalCleaned += recycleIds.size();
                    log.info("用户{}的{}条过期回收站文件已清理", userId, recycleIds.size());
                } catch (Exception e) {
                    log.error("清理用户{}的过期回收站文件失败: {}", userId, e.getMessage(), e);
                }
            }

            log.info("回收站自动清理完成，共清理{}条记录", totalCleaned);
        } catch (Exception e) {
            log.error("清理过期回收站文件失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}