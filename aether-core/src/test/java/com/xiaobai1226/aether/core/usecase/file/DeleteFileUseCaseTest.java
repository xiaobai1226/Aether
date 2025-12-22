// package com.xiaobai1226.aether.core.usecase.file;

// import com.xiaobai1226.aether.common.exception.FailResultException;
// import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
// import com.xiaobai1226.aether.core.service.intf.UserFileService;
// import com.xiaobai1226.aether.core.service.support.UserFileTreeService;
// import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
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
// import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.DEL;
// import static com.xiaobai1226.aether.core.enums.UserFileStatusEnum.NORMAL;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// /**
//  * DeleteFileUseCase 单元测试
//  *
//  * @author bai
//  */
// @ExtendWith(MockitoExtension.class)
// @DisplayName("文件删除用例测试")
// class DeleteFileUseCaseTest {

//     @Mock
//     private UserFileService userFileService;

//     @Mock
//     private RecycleBinService recycleBinService;

//     @Mock
//     private UserFileTreeService userFileTreeService;

//     @InjectMocks
//     private DeleteFileUseCase deleteFileUseCase;

//     private Long userId = 1L;
//     private Long fileId = 100L;
//     private UserFileTreeDTO fileTree;

//     @BeforeEach
//     void setUp() {
//         fileTree = new UserFileTreeDTO();
//         fileTree.setId(fileId);
//         fileTree.setName("test.txt");
//         fileTree.setParentId(0L);
//         fileTree.setItemType(FILE.flag());
//         fileTree.setUserId(userId);
//         fileTree.setSize(1024L);
//     }

//     @Test
//     @DisplayName("正常删除文件到回收站")
//     void testDeleteFileToRecycleBinSuccess() {
//         // Given
//         List<Long> ids = Arrays.asList(fileId);
//         List<UserFileTreeDTO> fileTreeList = Arrays.asList(fileTree);
        
//         when(userFileService.getUserFileTreeDTOByIdsAndUserId(ids, userId, NORMAL.flag()))
//                 .thenReturn(fileTreeList);
//         doNothing().when(userFileTreeService).recursiveGetUserFileTreeDTO(fileTreeList, userId);
//         doNothing().when(recycleBinService).insertBatch(anyList());
//         doNothing().when(userFileService).updateUserFileStatusById(anyList(), eq(userId), eq(DEL));

//         // When
//         deleteFileUseCase.execute(ids, userId);

//         // Then
//         verify(recycleBinService).insertBatch(anyList());
//         verify(userFileService).updateUserFileStatusById(anyList(), eq(userId), eq(DEL));
//     }

//     @Test
//     @DisplayName("删除文件时ID列表为空应抛出异常")
//     void testDeleteFileWithEmptyIds() {
//         // Given
//         List<Long> ids = List.of();

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             deleteFileUseCase.execute(ids, userId);
//         });

//         verify(recycleBinService, never()).insertBatch(any());
//         verify(userFileService, never()).updateUserFileStatusById(any(), any(), any());
//     }

//     @Test
//     @DisplayName("删除不存在的文件应抛出异常")
//     void testDeleteNonExistentFile() {
//         // Given
//         List<Long> ids = Arrays.asList(fileId);
        
//         when(userFileService.getUserFileTreeDTOByIdsAndUserId(ids, userId, NORMAL.flag()))
//                 .thenReturn(List.of()); // 返回空列表

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             deleteFileUseCase.execute(ids, userId);
//         });

//         verify(recycleBinService, never()).insertBatch(any());
//         verify(userFileService, never()).updateUserFileStatusById(any(), any(), any());
//     }

//     @Test
//     @DisplayName("删除文件夹及其子文件")
//     void testDeleteFolderWithChildren() {
//         // Given
//         Long folderId = 200L;
//         UserFileTreeDTO folderTree = new UserFileTreeDTO();
//         folderTree.setId(folderId);
//         folderTree.setName("folder");
//         folderTree.setParentId(0L);
//         folderTree.setItemType(FOLDER.flag());
//         folderTree.setUserId(userId);
        
//         // 添加子文件
//         UserFileTreeDTO childFile = new UserFileTreeDTO();
//         childFile.setId(201L);
//         childFile.setName("child.txt");
//         childFile.setParentId(folderId);
//         childFile.setItemType(FILE.flag());
//         childFile.setSize(512L);
//         folderTree.setChildren(Arrays.asList(childFile));

//         List<Long> ids = Arrays.asList(folderId);
//         List<UserFileTreeDTO> fileTreeList = Arrays.asList(folderTree);
        
//         when(userFileService.getUserFileTreeDTOByIdsAndUserId(ids, userId, NORMAL.flag()))
//                 .thenReturn(fileTreeList);
//         doNothing().when(userFileTreeService).recursiveGetUserFileTreeDTO(fileTreeList, userId);
//         doNothing().when(recycleBinService).insertBatch(anyList());
//         doNothing().when(userFileService).updateUserFileStatusById(anyList(), eq(userId), eq(DEL));
//         when(userFileTreeService.flattenUserFileTree(folderTree))
//                 .thenReturn(Arrays.asList(folderTree, childFile));

//         // When
//         deleteFileUseCase.execute(ids, userId);

//         // Then
//         verify(recycleBinService).insertBatch(anyList());
//         verify(userFileService).updateUserFileStatusById(argThat(list -> list.size() == 2), eq(userId), eq(DEL));
//     }

//     @Test
//     @DisplayName("批量删除多个文件")
//     void testBatchDeleteFiles() {
//         // Given
//         Long fileId2 = 101L;
//         UserFileTreeDTO fileTree2 = new UserFileTreeDTO();
//         fileTree2.setId(fileId2);
//         fileTree2.setName("test2.txt");
//         fileTree2.setParentId(0L);
//         fileTree2.setItemType(FILE.flag());
//         fileTree2.setUserId(userId);
//         fileTree2.setSize(2048L);

//         List<Long> ids = Arrays.asList(fileId, fileId2);
//         List<UserFileTreeDTO> fileTreeList = Arrays.asList(fileTree, fileTree2);
        
//         when(userFileService.getUserFileTreeDTOByIdsAndUserId(ids, userId, NORMAL.flag()))
//                 .thenReturn(fileTreeList);
//         doNothing().when(userFileTreeService).recursiveGetUserFileTreeDTO(fileTreeList, userId);
//         doNothing().when(recycleBinService).insertBatch(anyList());
//         doNothing().when(userFileService).updateUserFileStatusById(anyList(), eq(userId), eq(DEL));
//         when(userFileTreeService.flattenUserFileTree(fileTree))
//                 .thenReturn(Arrays.asList(fileTree));
//         when(userFileTreeService.flattenUserFileTree(fileTree2))
//                 .thenReturn(Arrays.asList(fileTree2));

//         // When
//         deleteFileUseCase.execute(ids, userId);

//         // Then
//         verify(recycleBinService).insertBatch(anyList());
//         verify(userFileService).updateUserFileStatusById(argThat(list -> list.size() == 2), eq(userId), eq(DEL));
//     }

//     @Test
//     @DisplayName("删除部分文件不存在应抛出异常")
//     void testDeletePartiallyNonExistentFiles() {
//         // Given
//         List<Long> ids = Arrays.asList(fileId, 999L); // 999L 不存在
//         List<UserFileTreeDTO> fileTreeList = Arrays.asList(fileTree); // 只返回一个
        
//         when(userFileService.getUserFileTreeDTOByIdsAndUserId(ids, userId, NORMAL.flag()))
//                 .thenReturn(fileTreeList);

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             deleteFileUseCase.execute(ids, userId);
//         });

//         verify(recycleBinService, never()).insertBatch(any());
//         verify(userFileService, never()).updateUserFileStatusById(any(), any(), any());
//     }
// }