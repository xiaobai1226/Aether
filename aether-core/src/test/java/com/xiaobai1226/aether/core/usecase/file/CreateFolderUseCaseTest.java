// package com.xiaobai1226.aether.core.usecase.file;

// import com.xiaobai1226.aether.common.exception.FailResultException;
// import com.xiaobai1226.aether.core.service.intf.UserFileService;
// import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import static com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum.FOLDER;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// /**
//  * CreateFolderUseCase 单元测试
//  *
//  * @author bai
//  */
// @ExtendWith(MockitoExtension.class)
// @DisplayName("创建文件夹用例测试")
// class CreateFolderUseCaseTest {

//     @Mock
//     private UserFileService userFileService;

//     @InjectMocks
//     private CreateFolderUseCase createFolderUseCase;

//     private Long userId = 1L;
//     private UserFileDO newFolder;

//     @BeforeEach
//     void setUp() {
//         newFolder = new UserFileDO();
//         newFolder.setId(100L);
//         newFolder.setName("new_folder");
//         newFolder.setParentId(0L);
//         newFolder.setItemType(FOLDER.flag());
//         newFolder.setUserId(userId);
//     }

//     @Test
//     @DisplayName("在根目录创建文件夹")
//     void testCreateFolderInRoot() {
//         // Given
//         String folderName = "new_folder";
        
//         when(userFileService.newFolder(folderName, null, userId))
//                 .thenReturn(newFolder);

//         // When
//         UserFileDO result = createFolderUseCase.execute(folderName, null, userId);

//         // Then
//         assertNotNull(result);
//         assertEquals(folderName, result.getName());
//         assertEquals(0L, result.getParentId());
//         verify(userFileService).newFolder(folderName, null, userId);
//     }

//     @Test
//     @DisplayName("在指定路径下创建文件夹")
//     void testCreateFolderInSpecifiedPath() {
//         // Given
//         String folderName = "sub_folder";
//         String parentPath = "/parent/path";
        
//         UserFileDO parentFolder = new UserFileDO();
//         parentFolder.setId(200L);
//         parentFolder.setName("path");
//         parentFolder.setItemType(FOLDER.flag());
        
//         UserFileDO newSubFolder = new UserFileDO();
//         newSubFolder.setId(101L);
//         newSubFolder.setName(folderName);
//         newSubFolder.setParentId(parentFolder.getId());
//         newSubFolder.setItemType(FOLDER.flag());
        
//         when(userFileService.getParentFolderByPath(userId, 0L, parentPath))
//                 .thenReturn(parentFolder);
//         when(userFileService.newFolder(folderName, parentFolder, userId))
//                 .thenReturn(newSubFolder);

//         // When
//         UserFileDO result = createFolderUseCase.execute(folderName, parentPath, userId);

//         // Then
//         assertNotNull(result);
//         assertEquals(folderName, result.getName());
//         assertEquals(parentFolder.getId(), result.getParentId());
//         verify(userFileService).getParentFolderByPath(userId, 0L, parentPath);
//         verify(userFileService).newFolder(folderName, parentFolder, userId);
//     }

//     @Test
//     @DisplayName("在不存在的路径下创建文件夹应抛出异常")
//     void testCreateFolderInNonExistentPath() {
//         // Given
//         String folderName = "new_folder";
//         String parentPath = "/nonexistent/path";
        
//         when(userFileService.getParentFolderByPath(userId, 0L, parentPath))
//                 .thenReturn(null);

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             createFolderUseCase.execute(folderName, parentPath, userId);
//         });

//         verify(userFileService, never()).newFolder(any(), any(), any());
//     }

//     @Test
//     @DisplayName("创建空名称文件夹应由Service层处理")
//     void testCreateFolderWithEmptyName() {
//         // Given
//         String folderName = "";
        
//         // Service层应该处理这种情况（抛出异常或使用默认名称）
//         when(userFileService.newFolder(folderName, null, userId))
//                 .thenThrow(new FailResultException(null, "文件夹名称不能为空"));

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             createFolderUseCase.execute(folderName, null, userId);
//         });
//     }

//     @Test
//     @DisplayName("创建同名文件夹应由Service层处理（自动重命名或抛出异常）")
//     void testCreateFolderWithDuplicateName() {
//         // Given
//         String folderName = "existing_folder";
        
//         // Service层应该处理重名情况
//         when(userFileService.newFolder(folderName, null, userId))
//                 .thenThrow(new FailResultException(null, "文件夹名称已存在"));

//         // When & Then
//         assertThrows(FailResultException.class, () -> {
//             createFolderUseCase.execute(folderName, null, userId);
//         });
//     }

//     @Test
//     @DisplayName("创建深层嵌套文件夹")
//     void testCreateDeeplyNestedFolder() {
//         // Given
//         String folderName = "deep_folder";
//         String parentPath = "/level1/level2/level3";
        
//         UserFileDO deepParent = new UserFileDO();
//         deepParent.setId(300L);
//         deepParent.setName("level3");
//         deepParent.setItemType(FOLDER.flag());
        
//         UserFileDO newDeepFolder = new UserFileDO();
//         newDeepFolder.setId(301L);
//         newDeepFolder.setName(folderName);
//         newDeepFolder.setParentId(deepParent.getId());
//         newDeepFolder.setItemType(FOLDER.flag());
        
//         when(userFileService.getParentFolderByPath(userId, 0L, parentPath))
//                 .thenReturn(deepParent);
//         when(userFileService.newFolder(folderName, deepParent, userId))
//                 .thenReturn(newDeepFolder);

//         // When
//         UserFileDO result = createFolderUseCase.execute(folderName, parentPath, userId);

//         // Then
//         assertNotNull(result);
//         assertEquals(folderName, result.getName());
//         assertEquals(deepParent.getId(), result.getParentId());
//     }
// }