package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.domain.dto.DirectLinkAccessDTO;
import com.xiaobai1226.aether.core.domain.dto.DirectLinkCreateResultDTO;
import com.xiaobai1226.aether.core.domain.dto.DirectLinkRecordDTO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.entity.DirectLinkDO;
import com.xiaobai1226.aether.dao.mapper.DirectLinkMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xiaobai1226.aether.common.constant.ResultErrorMsgConsts.*;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.PARAM_IS_INVALID;
import static com.xiaobai1226.aether.common.enums.ResultCodeEnum.SYSTEM_ERROR;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;

/**
 * 文件直链用例
 */
@Component
@Slf4j
public class DirectLinkUseCase {
    private static final int ENABLED_STATUS = 1;
    private static final int DISABLED_STATUS = 0;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Db
    private DirectLinkMapper directLinkMapper;

    @Inject
    private UserFileService userFileService;

    @Inject("${project.direct-link.default-expire-days:7}")
    private int defaultExpireDays;

    /**
     * 创建文件直链
     */
    public DirectLinkCreateResultDTO createDirectLink(Long userFileId, Integer expireDays, Long userId) {
        var userFileDO = userFileService.getUserFileByIdAndUserId(userFileId, userId, NORMAL);
        if (userFileDO == null || !UserFileItemTypeEnum.isFile(userFileDO.getItemType())) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_FILE_NO_EXIST);
        }

        int finalExpireDays = expireDays == null ? defaultExpireDays : expireDays;
        if (finalExpireDays < 0) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DIRECT_LINK_EXPIRE_DAYS_INVALID);
        }

        String token = RandomUtil.randomString(32);
        String expireAt = null;
        if (finalExpireDays > 0) {
            expireAt = LocalDateTime.now().plusDays(finalExpireDays).format(DATE_TIME_FORMATTER);
        }

        var insertCount = directLinkMapper.insert(new DirectLinkDO()
                .setToken(token)
                .setUserFileId(userFileId)
                .setUserId(userId)
                .setStatus(ENABLED_STATUS)
                .setExpireAt(expireAt));
        if (insertCount != 1) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var result = new DirectLinkCreateResultDTO();
        result.setToken(token);
        result.setExpireAt(expireAt);
        return result;
    }

    /**
     * 撤销文件直链
     */
    public void revokeDirectLink(String token, Long userId) {
        var directLinkDO = directLinkMapper.selectOne(new LambdaQueryWrapper<DirectLinkDO>()
                .eq(DirectLinkDO::getToken, token)
                .eq(DirectLinkDO::getUserId, userId)
                .eq(DirectLinkDO::getStatus, ENABLED_STATUS));
        if (directLinkDO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DIRECT_LINK_NO_EXIST);
        }

        var updateCount = directLinkMapper.update(null, new LambdaUpdateWrapper<DirectLinkDO>()
                .eq(DirectLinkDO::getId, directLinkDO.getId())
                .set(DirectLinkDO::getStatus, DISABLED_STATUS));
        if (updateCount != 1) {
            throw new FailResultException(SYSTEM_ERROR);
        }
    }

    /**
     * 校验直链并返回访问上下文
     */
    public DirectLinkAccessDTO checkAndGetAccessContext(String token) {
        var directLinkDO = directLinkMapper.selectOne(new LambdaQueryWrapper<DirectLinkDO>()
                .eq(DirectLinkDO::getToken, token)
                .eq(DirectLinkDO::getStatus, ENABLED_STATUS));
        if (directLinkDO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DIRECT_LINK_NO_EXIST);
        }

        if (isExpired(directLinkDO.getExpireAt())) {
            directLinkMapper.update(null, new LambdaUpdateWrapper<DirectLinkDO>()
                    .eq(DirectLinkDO::getId, directLinkDO.getId())
                    .set(DirectLinkDO::getStatus, DISABLED_STATUS));
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DIRECT_LINK_NO_EXIST);
        }

        return new DirectLinkAccessDTO(directLinkDO.getUserFileId(), directLinkDO.getUserId());
    }

    /**
     * 分页获取文件直链记录
     */
    public PageResult<DirectLinkRecordDTO> getDirectLinkListByPage(Integer pageNum, Integer pageSize, Long userId) {
        var page = new Page<DirectLinkDO>(pageNum, pageSize);
        var directLinkPage = directLinkMapper.selectPage(page, new LambdaQueryWrapper<DirectLinkDO>()
                .eq(DirectLinkDO::getUserId, userId)
                .orderByDesc(DirectLinkDO::getCreateTime));

        var recordList = directLinkPage.getRecords();
        if (recordList == null || recordList.isEmpty()) {
            return PageResult.with(new Page<DirectLinkRecordDTO>(pageNum, pageSize, 0));
        }

        List<Long> userFileIds = recordList.stream().map(DirectLinkDO::getUserFileId).distinct().collect(Collectors.toList());
        Map<Long, com.xiaobai1226.aether.dao.domain.dto.UserFileDTO> userFileMap = new HashMap<>();
        Map<Long, String> folderPathCache = new HashMap<>();
        var userFileList = userFileService.getUserFileDTOListByIds(userFileIds, userId, NORMAL);
        if (userFileList != null) {
            for (var userFile : userFileList) {
                if (userFile.getId() != null) {
                    userFileMap.put(userFile.getId(), userFile);
                }
            }
        }

        var dtoPage = new Page<DirectLinkRecordDTO>(directLinkPage.getCurrent(), directLinkPage.getSize(), directLinkPage.getTotal());
        dtoPage.setRecords(recordList.stream().map(record -> {
            var dto = new DirectLinkRecordDTO();
            dto.setToken(record.getToken());
            dto.setUserFileId(record.getUserFileId());
            dto.setStatus(record.getStatus());
            dto.setExpireAt(record.getExpireAt());
            dto.setCreateTime(record.getCreateTime());

            var userFile = userFileMap.get(record.getUserFileId());
            if (userFile == null) {
                dto.setFileName("文件已删除");
                dto.setFolderPath("-");
            } else {
                dto.setFileName(userFile.getName());
                dto.setSuffix(userFile.getSuffix());
                dto.setFolderPath(resolveFolderPathByParentId(userFile.getParentId(), userId, folderPathCache));
            }

            dto.setBizStatus(resolveBizStatus(record));
            return dto;
        }).collect(Collectors.toList()));

        return PageResult.with(dtoPage);
    }

    /**
     * 更新直链有效期
     */
    public DirectLinkCreateResultDTO updateDirectLinkExpire(String token, Integer expireDays, Long userId) {
        if (expireDays == null || expireDays < 0) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DIRECT_LINK_EXPIRE_DAYS_INVALID);
        }

        var directLinkDO = directLinkMapper.selectOne(new LambdaQueryWrapper<DirectLinkDO>()
                .eq(DirectLinkDO::getToken, token)
                .eq(DirectLinkDO::getUserId, userId));
        if (directLinkDO == null) {
            throw new FailResultException(PARAM_IS_INVALID, ERROR_DIRECT_LINK_NO_EXIST);
        }

        String expireAt = null;
        if (expireDays > 0) {
            expireAt = LocalDateTime.now().plusDays(expireDays).format(DATE_TIME_FORMATTER);
        }

        var updateCount = directLinkMapper.update(null, new LambdaUpdateWrapper<DirectLinkDO>()
                .eq(DirectLinkDO::getId, directLinkDO.getId())
                .set(DirectLinkDO::getExpireAt, expireAt));
        if (updateCount != 1) {
            throw new FailResultException(SYSTEM_ERROR);
        }

        var result = new DirectLinkCreateResultDTO();
        result.setToken(token);
        result.setExpireAt(expireAt);
        return result;
    }

    private boolean isExpired(String expireAt) {
        if (expireAt == null || expireAt.isBlank()) {
            return false;
        }
        try {
            LocalDateTime expireTime = LocalDateTime.parse(expireAt, DATE_TIME_FORMATTER);
            return LocalDateTime.now().isAfter(expireTime);
        } catch (Exception e) {
            log.warn("解析直链过期时间失败，按已过期处理: expireAt={}", expireAt);
            return true;
        }
    }

    private String resolveBizStatus(DirectLinkDO directLinkDO) {
        if (directLinkDO.getStatus() == null || directLinkDO.getStatus() != ENABLED_STATUS) {
            return "DISABLED";
        }
        if (isExpired(directLinkDO.getExpireAt())) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }

    /**
     * 解析用户可见目录路径（与文件列表路径口径一致）
     */
    private String resolveFolderPathByParentId(Long parentId, Long userId, Map<Long, String> folderPathCache) {
        if (parentId == null || parentId == 0L) {
            return "/";
        }

        if (folderPathCache.containsKey(parentId)) {
            return folderPathCache.get(parentId);
        }

        List<String> folderNames = new ArrayList<>();
        Long currentParentId = parentId;
        while (currentParentId != null && currentParentId > 0) {
            var folder = userFileService.getUserFileByIdAndUserId(currentParentId, userId, NORMAL);
            if (folder == null || !UserFileItemTypeEnum.isFolder(folder.getItemType())) {
                break;
            }
            folderNames.add(folder.getName());
            currentParentId = folder.getParentId();
        }

        if (folderNames.isEmpty()) {
            folderPathCache.put(parentId, "/");
            return "/";
        }

        java.util.Collections.reverse(folderNames);
        String folderPath = "/" + String.join("/", folderNames);
        folderPathCache.put(parentId, folderPath);
        return folderPath;
    }
}