package com.xiaobai1226.aether.core.usecase.file;

import cn.hutool.core.collection.CollUtil;
import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.QuotaService;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.core.service.support.UserFileTreeService;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FOLDER;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CopyFileUseCase 单元测试
 *
 * @author bai
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文件复制用例测试")
class CopyFileUseCaseTest {

    @Mock
    private UserFileService userFileService;

    @Mock
    private QuotaService quotaService;

    @Mock
    private UserFileTreeService userFileTreeService;

    @InjectMocks
    private CopyFileUseCase copyFileUseCase;

    private Long userId = 1L;
    private Long sourceFileId = 100L;
    private Long targetFolderId = 200L;
    private UserFileTreeDTO sourceFileTree;
    private UserFileDO targetFolder;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        sourceFileTree = new UserFileTreeDTO();
        sourceFileTree.setId(sourceFileId);
        sourceFileTree.setName("test.txt");
        sourceFileTree.setParentId(0L);
        sourceFileTree.setItemType(FILE.flag());
        sourceFileTree.setUserId(userId);
        sourceFileTree.setSize(1024L);

        targetFolder = new UserFileDO();
        targetFolder.setId(targetFolderId);
        targetFolder.setName("target");
        targetFolder.setItemType(FOLDER.flag());
        targetFolder.setUserId(userId);
    }

    @Test
    @DisplayName("正常复制文件到文件夹")
    void testCopyFileToFolderSuccess() {
        // Given
        List<Long> sourceIds = Arrays.asList(sourceFileId);
        List<UserFileTreeDTO> sourceTreeList = Arrays.asList(sourceFileTree);
        
        when(userFileService.getUserFileTreeDTOByIdsAndUserId(sourceIds, userId, NORMAL.flag()))
                .thenReturn(sourceTreeList);
        when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
                .thenReturn(targetFolder);
        when(userFileService.getAllSubfolders(userId, List.of())).thenReturn(List.of());
        when(userFileService.getCountByNames(anyList(), eq(userId), eq(targetFolderId), eq(NORMAL), eq(FILE)))
                .thenReturn(0L);
        when(userFileTreeService.getUserFileTreeSpaceUsage(sourceTreeList))
                .thenReturn(1024L);
        doNothing().when(quotaService).checkEnough(userId, 1024L);

        // When
        copyFileUseCase.execute(sourceIds, targetFolderId, userId);

        // Then
        verify(quotaService).checkEnough(userId, 1024L);
        verify(userFileService).copy(targetFolderId, userId, sourceTreeList, 1024L);
    }

    @Test
    @DisplayName("复制文件时源文件列表为空应抛出异常")
    void testCopyFileWithEmptySourceIds() {
        // Given
        List<Long> sourceIds = List.of();

        // When & Then
        assertThrows(FailResultException.class, () -> {
            copyFileUseCase.execute(sourceIds, targetFolderId, userId);
        });

        verify(userFileService, never()).copy(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("复制文件到不存在的目标文件夹应抛出异常")
    void testCopyFileToNonExistentFolder() {
        // Given
        List<Long> sourceIds = Arrays.asList(sourceFileId);
        List<UserFileTreeDTO> sourceTreeList = Arrays.asList(sourceFileTree);
        
        when(userFileService.getUserFileTreeDTOByIdsAndUserId(sourceIds, userId, NORMAL.flag()))
                .thenReturn(sourceTreeList);
        when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
                .thenReturn(null);

        // When & Then
        assertThrows(FailResultException.class, () -> {
            copyFileUseCase.execute(sourceIds, targetFolderId, userId);
        });

        verify(userFileService, never()).copy(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("复制到当前文件夹应抛出异常")
    void testCopyToCurrentFolder() {
        // Given
        List<Long> sourceIds = Arrays.asList(sourceFileId);
        List<UserFileTreeDTO> sourceTreeList = Arrays.asList(sourceFileTree);
        sourceFileTree.setParentId(targetFolderId); // 源文件已经在目标文件夹中
        
        when(userFileService.getUserFileTreeDTOByIdsAndUserId(sourceIds, userId, NORMAL.flag()))
                .thenReturn(sourceTreeList);
        when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
                .thenReturn(targetFolder);

        // When & Then
        assertThrows(FailResultException.class, () -> {
            copyFileUseCase.execute(sourceIds, targetFolderId, userId);
        });

        verify(userFileService, never()).copy(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("复制文件到存在同名文件的目标文件夹应抛出异常")
    void testCopyToFolderWithSameName() {
        // Given
        List<Long> sourceIds = Arrays.asList(sourceFileId);
        List<UserFileTreeDTO> sourceTreeList = Arrays.asList(sourceFileTree);
        
        when(userFileService.getUserFileTreeDTOByIdsAndUserId(sourceIds, userId, NORMAL.flag()))
                .thenReturn(sourceTreeList);
        when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
                .thenReturn(targetFolder);
        when(userFileService.getAllSubfolders(userId, List.of())).thenReturn(List.of());
        when(userFileService.getCountByNames(anyList(), eq(userId), eq(targetFolderId), eq(NORMAL), eq(FILE)))
                .thenReturn(1L); // 已存在同名文件

        // When & Then
        assertThrows(FailResultException.class, () -> {
            copyFileUseCase.execute(sourceIds, targetFolderId, userId);
        });

        verify(userFileService, never()).copy(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("复制文件时空间不足应抛出异常")
    void testCopyFileWithInsufficientSpace() {
        // Given
        List<Long> sourceIds = Arrays.asList(sourceFileId);
        List<UserFileTreeDTO> sourceTreeList = Arrays.asList(sourceFileTree);
        
        when(userFileService.getUserFileTreeDTOByIdsAndUserId(sourceIds, userId, NORMAL.flag()))
                .thenReturn(sourceTreeList);
        when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
                .thenReturn(targetFolder);
        when(userFileService.getAllSubfolders(userId, List.of())).thenReturn(List.of());
        when(userFileService.getCountByNames(anyList(), eq(userId), eq(targetFolderId), eq(NORMAL), eq(FILE)))
                .thenReturn(0L);
        when(userFileTreeService.getUserFileTreeSpaceUsage(sourceTreeList))
                .thenReturn(1024L);
        doThrow(new FailResultException(null, "空间不足"))
                .when(quotaService).checkEnough(userId, 1024L);

        // When & Then
        assertThrows(FailResultException.class, () -> {
            copyFileUseCase.execute(sourceIds, targetFolderId, userId);
        });

        verify(userFileService, never()).copy(any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("复制文件夹及其子文件")
    void testCopyFolderWithChildren() {
        // Given
        Long folderTreeId = 300L;
        UserFileTreeDTO folderTree = new UserFileTreeDTO();
        folderTree.setId(folderTreeId);
        folderTree.setName("folder");
        folderTree.setParentId(0L);
        folderTree.setItemType(FOLDER.flag());
        folderTree.setUserId(userId);
        folderTree.setSize(0L);
        
        // 添加子文件
        UserFileTreeDTO childFile = new UserFileTreeDTO();
        childFile.setId(301L);
        childFile.setName("child.txt");
        childFile.setParentId(folderTreeId);
        childFile.setItemType(FILE.flag());
        childFile.setSize(512L);
        folderTree.setChildren(Arrays.asList(childFile));

        List<Long> sourceIds = Arrays.asList(folderTreeId);
        List<UserFileTreeDTO> sourceTreeList = Arrays.asList(folderTree);
        
        when(userFileService.getUserFileTreeDTOByIdsAndUserId(sourceIds, userId, NORMAL.flag()))
                .thenReturn(sourceTreeList);
        when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
                .thenReturn(targetFolder);
        when(userFileService.getAllSubfolders(userId, Arrays.asList(folderTreeId)))
                .thenReturn(List.of());
        when(userFileService.getCountByNames(anyList(), eq(userId), eq(targetFolderId), eq(NORMAL), eq(FOLDER)))
                .thenReturn(0L);
        when(userFileTreeService.getUserFileTreeSpaceUsage(sourceTreeList))
                .thenReturn(512L); // 只有子文件占用空间
        doNothing().when(quotaService).checkEnough(userId, 512L);

        // When
        copyFileUseCase.execute(sourceIds, targetFolderId, userId);

        // Then
        verify(quotaService).checkEnough(userId, 512L);
        verify(userFileService).copy(targetFolderId, userId, sourceTreeList, 512L);
    }
}