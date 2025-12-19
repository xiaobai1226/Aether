package com.xiaobai1226.aether.core.usecase.file;

import com.xiaobai1226.aether.common.exception.FailResultException;
import com.xiaobai1226.aether.core.service.intf.UserFileService;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RenameFileUseCase 单元测试
 *
 * @author bai
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文件重命名用例测试")
class RenameFileUseCaseTest {

    @Mock
    private UserFileService userFileService;

    @InjectMocks
    private RenameFileUseCase renameFileUseCase;

    private Long userId = 1L;
    private Long fileId = 100L;
    private UserFileDO existingFile;

    @BeforeEach
    void setUp() {
        existingFile = new UserFileDO();
        existingFile.setId(fileId);
        existingFile.setName("old_name.txt");
        existingFile.setParentId(0L);
        existingFile.setItemType(FILE.flag());
        existingFile.setUserId(userId);
    }

    @Test
    @DisplayName("正常重命名文件")
    void testRenameFileSuccess() {
        // Given
        String newName = "new_name.txt";
        
        when(userFileService.getUserFileByIdAndUserId(fileId, userId, NORMAL))
                .thenReturn(existingFile);
        when(userFileService.getUserFileByName(newName, userId, existingFile.getParentId(), NORMAL))
                .thenReturn(null); // 不存在同名文件
        when(userFileService.rename(fileId, userId, newName, existingFile, NORMAL))
                .thenReturn(true);

        // When
        renameFileUseCase.execute(fileId, newName, userId);

        // Then
        verify(userFileService).rename(fileId, userId, newName, existingFile, NORMAL);
    }

    @Test
    @DisplayName("重命名不存在的文件应抛出异常")
    void testRenameNonExistentFile() {
        // Given
        String newName = "new_name.txt";
        when(userFileService.getUserFileByIdAndUserId(fileId, userId, NORMAL))
                .thenReturn(null);

        // When & Then
        assertThrows(FailResultException.class, () -> {
            renameFileUseCase.execute(fileId, newName, userId);
        });

        verify(userFileService, never()).rename(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("重命名为已存在的名称应抛出异常")
    void testRenameToExistingName() {
        // Given
        String newName = "existing_name.txt";
        UserFileDO conflictFile = new UserFileDO();
        conflictFile.setId(101L);
        conflictFile.setName(newName);
        
        when(userFileService.getUserFileByIdAndUserId(fileId, userId, NORMAL))
                .thenReturn(existingFile);
        when(userFileService.getUserFileByName(newName, userId, existingFile.getParentId(), NORMAL))
                .thenReturn(conflictFile); // 已存在同名文件

        // When & Then
        assertThrows(FailResultException.class, () -> {
            renameFileUseCase.execute(fileId, newName, userId);
        });

        verify(userFileService, never()).rename(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("重命名为相同名称应成功（无变化）")
    void testRenameToSameName() {
        // Given
        String sameName = "old_name.txt";
        
        when(userFileService.getUserFileByIdAndUserId(fileId, userId, NORMAL))
                .thenReturn(existingFile);
        when(userFileService.getUserFileByName(sameName, userId, existingFile.getParentId(), NORMAL))
                .thenReturn(existingFile); // 返回的是同一个文件
        when(userFileService.rename(fileId, userId, sameName, existingFile, NORMAL))
                .thenReturn(true);

        // When
        renameFileUseCase.execute(fileId, sameName, userId);

        // Then
        verify(userFileService).rename(fileId, userId, sameName, existingFile, NORMAL);
    }

    @Test
    @DisplayName("重命名操作失败应抛出异常")
    void testRenameOperationFailure() {
        // Given
        String newName = "new_name.txt";
        
        when(userFileService.getUserFileByIdAndUserId(fileId, userId, NORMAL))
                .thenReturn(existingFile);
        when(userFileService.getUserFileByName(newName, userId, existingFile.getParentId(), NORMAL))
                .thenReturn(null);
        when(userFileService.rename(fileId, userId, newName, existingFile, NORMAL))
                .thenReturn(false); // 重命名失败

        // When & Then
        assertThrows(FailResultException.class, () -> {
            renameFileUseCase.execute(fileId, newName, userId);
        });
    }

    @Test
    @DisplayName("重命名文件夹")
    void testRenameFolderSuccess() {
        // Given
        String newName = "new_folder";
        existingFile.setName("old_folder");
        existingFile.setItemType((byte) 1); // 文件夹
        
        when(userFileService.getUserFileByIdAndUserId(fileId, userId, NORMAL))
                .thenReturn(existingFile);
        when(userFileService.getUserFileByName(newName, userId, existingFile.getParentId(), NORMAL))
                .thenReturn(null);
        when(userFileService.rename(fileId, userId, newName, existingFile, NORMAL))
                .thenReturn(true);

        // When
        renameFileUseCase.execute(fileId, newName, userId);

        // Then
        verify(userFileService).rename(fileId, userId, newName, existingFile, NORMAL);
    }
}