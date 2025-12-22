// package com.xiaobai1226.aether.core.usecase.file;

// import com.xiaobai1226.aether.common.exception.FailResultException;
// import com.xiaobai1226.aether.core.service.impl.StorageMigrationService;
// import com.xiaobai1226.aether.core.service.intf.UserFileService;
// import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.util.Arrays;
// import java.util.List;

// import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FILE;
// import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FOLDER;
// import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// /**
//  * MoveFileUseCase 单元测试
//  *
//  * @author bai
//  */
// @ExtendWith(MockitoExtension.class)
// @DisplayName("文件移动用例测试")
// class MoveFileUseCaseTest {

//     @Mock
//     private UserFileService userFileService;

//     @Mock
//     private StorageMigrationService storageMigrationService;

//     @InjectMocks
//     private MoveFileUseCase moveFileUseCase;

//     private Long userId = 1L;
//     private Long sourceFileId = 100L;
//     private Long targetFolderId = 200L;
//     private UserFileDO sourceFile;
//     private UserFileDO targetFolder;

//     @BeforeEach
//     void setUp() {
//         // 初始化测试数据
//         sourceFile = new UserFileDO();
//         sourceFile.setId(sourceFileId);
//         sourceFile.setName("test.txt");
//         sourceFile.setParentId(0L);
//         sourceFile.setItemType(FILE.flag());
//         sourceFile.setUserId(userId);
//         sourceFile.setStorageSourceId(1L);
//         sourceFile.setStorageSourceType(1); // 继承

//         targetFolder = new UserFileDO();
//         targetFolder.setId(targetFolderId);
//         targetFolder.setName("target");
//         targetFolder.setItemType(FOLDER.flag());
//         targetFolder.setUserId(userId);
//         targetFolder.setStorageSourceId(2L);
//     }

//     @Test
//     @DisplayName("正常移动文件到文件夹")
//     void testMoveFileToFolderSuccess() {
//         // Given
//         List<Long> sourceIds = Arrays.asList(sourceFileId);
//         when(userFileService.getUserFileByIdsAndUserId(sourceIds, userId, NORMAL))
//                 .thenReturn(Arrays.asList(sourceFile));
//         when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
//                 .thenReturn(targetFolder);
//         when(userFileService.getAllSubfolders(userId, List.of())).thenReturn(List.of());
//         when(userFileService.getCountByNames(anyList(), eq(userId), eq(targetFolderId), eq(NORMAL), eq(FILE)))
//                 .thenReturn(0L);

//         // When
//         moveFileUseCase.execute(sourceIds, targetFolderId, userId);

//         // Then
//         verify(userFileService).updateParentIdByIds(sourceIds, targetFolderId, userId, NORMAL);
//         verify(userFileService).getUserFileByIdAndUserId(sourceFileId, userId, NORMAL);
//     }

//     @Test
//     @DisplayName("移动文件时源文件列表为空应抛出异常")
//     void testMoveFileWithEmptySourceIds() {
//         // Given
//         List<Long> sourceIds = List.of();

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             moveFileUseCase.execute(sourceIds, targetFolderId, userId);
//         });

//         verify(userFileService, never()).updateParentIdByIds(any(), any(), any(), any());
//     }

//     @Test
//     @DisplayName("移动文件到不存在的目标文件夹应抛出异常")
//     void testMoveFileToNonExistentFolder() {
//         // Given
//         List<Long> sourceIds = Arrays.asList(sourceFileId);
//         when(userFileService.getUserFileByIdsAndUserId(sourceIds, userId, NORMAL))
//                 .thenReturn(Arrays.asList(sourceFile));
//         when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
//                 .thenReturn(null);

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             moveFileUseCase.execute(sourceIds, targetFolderId, userId);
//         });

//         verify(userFileService, never()).updateParentIdByIds(any(), any(), any(), any());
//     }

//     @Test
//     @DisplayName("移动到当前文件夹应抛出异常")
//     void testMoveToCurrentFolder() {
//         // Given
//         List<Long> sourceIds = Arrays.asList(sourceFileId);
//         sourceFile.setParentId(targetFolderId); // 源文件已经在目标文件夹中
        
//         when(userFileService.getUserFileByIdsAndUserId(sourceIds, userId, NORMAL))
//                 .thenReturn(Arrays.asList(sourceFile));
//         when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
//                 .thenReturn(targetFolder);

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             moveFileUseCase.execute(sourceIds, targetFolderId, userId);
//         });

//         verify(userFileService, never()).updateParentIdByIds(any(), any(), any(), any());
//     }

//     @Test
//     @DisplayName("移动文件到存在同名文件的目标文件夹应抛出异常")
//     void testMoveToFolderWithSameName() {
//         // Given
//         List<Long> sourceIds = Arrays.asList(sourceFileId);
//         when(userFileService.getUserFileByIdsAndUserId(sourceIds, userId, NORMAL))
//                 .thenReturn(Arrays.asList(sourceFile));
//         when(userFileService.getUserFileByIdAndUserId(targetFolderId, userId, NORMAL))
//                 .thenReturn(targetFolder);
//         when(userFileService.getAllSubfolders(userId, List.of())).thenReturn(List.of());
//         when(userFileService.getCountByNames(anyList(), eq(userId), eq(targetFolderId), eq(NORMAL), eq(FILE)))
//                 .thenReturn(1L); // 已存在同名文件

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             moveFileUseCase.execute(sourceIds, targetFolderId, userId);
//         });

//         verify(userFileService, never()).updateParentIdByIds(any(), any(), any(), any());
//     }

//     @Test
//     @DisplayName("移动文件夹到其子文件夹应抛出异常")
//     void testMoveFolderToItsSubfolder() {
//         // Given
//         Long sourceFolderId = 300L;
//         Long subFolderId = 301L;
        
//         UserFileDO sourceFolder = new UserFileDO();
//         sourceFolder.setId(sourceFolderId);
//         sourceFolder.setName("source");
//         sourceFolder.setParentId(0L);
//         sourceFolder.setItemType(FOLDER.flag());
//         sourceFolder.setUserId(userId);
        
//         UserFileDO subFolder = new UserFileDO();
//         subFolder.setId(subFolderId);
//         subFolder.setName("sub");
//         subFolder.setItemType(FOLDER.flag());
//         subFolder.setUserId(userId);

//         List<Long> sourceIds = Arrays.asList(sourceFolderId);
//         when(userFileService.getUserFileByIdsAndUserId(sourceIds, userId, NORMAL))
//                 .thenReturn(Arrays.asList(sourceFolder));
//         when(userFileService.getUserFileByIdAndUserId(subFolderId, userId, NORMAL))
//                 .thenReturn(subFolder);
//         when(userFileService.getAllSubfolders(userId, Arrays.asList(sourceFolderId)))
//                 .thenReturn(Arrays.asList(subFolderId)); // subFolderId 是 sourceFolderId 的子文件夹

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             moveFileUseCase.execute(sourceIds, subFolderId, userId);
//         });

//         verify(userFileService, never()).updateParentIdByIds(any(), any(), any(), any());
//     }

//     @Test
//     @DisplayName("移动文件到根目录")
//     void testMoveFileToRootFolder() {
//         // Given
//         List<Long> sourceIds = Arrays.asList(sourceFileId);
//         sourceFile.setParentId(200L); // 当前在某个文件夹中
        
//         when(userFileService.getUserFileByIdsAndUserId(sourceIds, userId, NORMAL))
//                 .thenReturn(Arrays.asList(sourceFile));
//         when(userFileService.getAllSubfolders(userId, List.of())).thenReturn(List.of());
//         when(userFileService.getCountByNames(anyList(), eq(userId), eq(0L), eq(NORMAL), eq(FILE)))
//                 .thenReturn(0L);
//         when(userFileService.getDefaultStorageSourceId(userId)).thenReturn(1L);

//         // When
//         moveFileUseCase.execute(sourceIds, 0L, userId);

//         // Then
//         verify(userFileService).updateParentIdByIds(sourceIds, 0L, userId, NORMAL);
//     }
// }