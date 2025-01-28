package com.xiaobai1226.aether.core.service.intf;

import com.baomidou.mybatisplus.solon.service.IService;
import com.xiaobai1226.aether.core.domain.dto.*;
import com.xiaobai1226.aether.domain.dto.common.PageResult;
import com.xiaobai1226.aether.domain.entity.FileDO;
import com.xiaobai1226.aether.domain.entity.UserFileDO;
import com.xiaobai1226.aether.core.domain.vo.UploadFileVO;
import com.xiaobai1226.aether.core.domain.vo.UserFileVO;
import com.xiaobai1226.aether.core.domain.vo.UserFolderVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.enums.UserFileStatusEnum;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.UploadedFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件操作service
 *
 * @author bai
 */
public interface UserFileService extends IService<UserFileDO> {

    /**
     * 根据path获取父文件夹数据
     *
     * @param userId   用户ID
     * @param parentId 父ID
     * @param path     所属文件夹路径
     * @return 文件夹数据
     * @author bai
     */
    UserFileDO getParentFolderByPath(Integer userId, Integer parentId, String path);

    /**
     * 根据path获取父文件夹ID，如果不存在则创建这个文件夹
     *
     * @param userId   用户ID
     * @param parentId 父ID
     * @param path     所属文件夹路径
     * @return 文件夹ID
     * @author bai
     */
    Integer getParentFolderByPathOrCreate(Integer userId, Integer parentId, String path);

    /**
     * 根据path获取用户文件数据
     *
     * @param userId 用户ID
     * @param path   所属文件夹路径
     * @return 文件夹数据
     * @author bai
     */
    UserFileDTO getUserFileDTOByPath(Integer userId, String path);

    /**
     * 获取文件及文件夹列表
     *
     * @param userId     用户ID
     * @param parentId   父ID
     * @param userFileVO 用户文件信息
     * @return 获取到的用户文件数据
     * @author bai
     */
    PageResult<UserFileDTO> getFileList(Integer userId, Integer parentId, UserFileVO userFileVO);

    /**
     * 根据文件名称获取文件数据
     *
     * @param fileName       文件或文件夹名称
     * @param parentId       父ID
     * @param userId         用户ID
     * @param userFileStatus 文件状态
     *                       //     * @param itemType       文件类型
     * @return 文件夹数据
     * @author bai
     */
//    UserFileDO getUserFileByName(String fileName, Integer userId, Integer parentId, UserFileStatusEnum userFileStatus, UserFileItemTypeEnum itemType);
    UserFileDO getUserFileByName(String fileName, Integer userId, Integer parentId, UserFileStatusEnum userFileStatus);

    /**
     * 新建文件夹
     *
     * @param folderName 文件夹名称
     * @param parentId   父ID
     * @param userId     用户ID
     * @return 文件夹ID
     * @author bai
     */
    Integer newFolder(String folderName, Integer parentId, Integer userId);

    /**
     * 根据文件ID获取文件数据
     *
     * @param id             文件或文件夹ID
     * @param userId         用户ID
     * @param userFileStatus 文件状态 null 全部
     * @return 文件夹数据
     * @author bai
     */
    UserFileDO getUserFileByIdAndUserId(Integer id, Integer userId, UserFileStatusEnum userFileStatus);

    /**
     * 重命名
     *
     * @param id             文件或文件夹ID
     * @param userId         用户ID
     * @param newName        新名称
     * @param userFileStatus 文件状态
     * @return 修改结果
     */
    Boolean updateFileNameById(Integer id, Integer userId, String newName, UserFileStatusEnum userFileStatus);

    /**
     * 重命名
     *
     * @param id             文件或文件夹ID
     * @param userId         用户ID
     * @param newName        新名称
     * @param userFileDO     要修改的文件信息
     * @param userFileStatus 文件状态
     * @return 修改结果
     */
    Boolean rename(Integer id, Integer userId, String newName, UserFileDO userFileDO, UserFileStatusEnum userFileStatus);

    /**
     * 秒传文件（当文件已存在时使用）
     *
     * @param userId       用户ID
     * @param parentId     父ID
     * @param uploadFileVO 上传文件相关信息
     * @param fileDO       文件信息
     * @return 上传结果
     * @author bai
     */
    UploadResultDTO secondUploadFile(Integer userId, Integer parentId, UploadFileVO uploadFileVO, FileDO fileDO);

    /**
     * 分片上传文件
     *
     * @param file               上传的文件
     * @param userId             用户ID
     * @param parentId           父ID
     * @param uploadFileVO       上传文件相关信息
     * @param uploadFileCacheDTO 上传文件缓存相关信息
     * @return 上传结果
     * @author bai
     */
    UploadResultDTO splitUploadFile(UploadedFile file, Integer userId, Integer parentId, UploadFileVO uploadFileVO, UploadFileCacheDTO uploadFileCacheDTO) throws IOException;

    /**
     * 取消上传
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @author bai
     */
    void cancelUploadFile(Integer userId, String taskId);

    /**
     * 清除缓存数据
     *
     * @param userId             用户ID
     * @param taskId             任务ID
     * @param fileSize           文件大小
     * @param uploadFileCacheDTO 上传文件缓存相关信息
     * @author bai
     */
    void clearUploadFileCache(Integer userId, String taskId, Long fileSize, UploadFileCacheDTO uploadFileCacheDTO);

    /**
     * 获取文件夹列表
     *
     * @param userId       用户ID
     * @param parentId     父ID
     * @param userFolderVO 用户文件夹信息
     * @return 查询到结果
     * @author bai
     */
    PageResult<UserFileDO> getFolderList(Integer userId, Integer parentId, UserFolderVO userFolderVO);

    /**
     * 根据文件ID集合获取文件数据
     *
     * @param ids            文件或文件夹ID集合
     * @param userId         用户ID
     * @param userFileStatus 文件状态
     * @return 文件夹数据
     * @author bai
     */
    List<UserFileDO> getUserFileByIdsAndUserId(List<Integer> ids, Integer userId, UserFileStatusEnum userFileStatus);

    /**
     * 获取所有子文件夹（包括子文件夹的子文件夹）
     *
     * @param userId 用户ID
     * @param ids    文件或文件夹ID集合
     */
    List<Integer> getAllSubfolders(Integer userId, List<Integer> ids);

    /**
     * 根据文件名称获取文件数量
     *
     * @param fileNames      文件或文件夹名称集合
     * @param parentId       父ID
     * @param userId         用户ID
     * @param userFileStatus 文件状态
     * @param itemType       文件类型
     * @return 文件夹数据
     * @author bai
     */
    Long getCountByNames(List<String> fileNames, Integer userId, Integer parentId, UserFileStatusEnum userFileStatus, UserFileItemTypeEnum itemType);

    /**
     * 根据id集合修改parentId
     *
     * @param sourceIds      源文件或文件夹ID集合
     * @param targetId       目标文件夹ID
     * @param userId         用户ID
     * @param userFileStatus 文件状态
     */
    void updateParentIdByIds(List<Integer> sourceIds, Integer targetId, Integer userId, UserFileStatusEnum userFileStatus);

    /**
     * 根据文件ID集合获取文件集合
     *
     * @param ids            文件或文件夹ID集合
     * @param userId         用户ID
     * @param userFileStatus 文件状态
     * @return 文件夹数据
     * @author bai
     */
    List<UserFileDTO> getUserFileDTOListByIds(List<Integer> ids, Integer userId, UserFileStatusEnum userFileStatus);

    /**
     * 根据文件ID集合获取文件树集合
     *
     * @param ids            文件或文件夹ID集合
     * @param userId         用户ID
     * @param userFileStatus 文件状态
     * @return 文件夹数据
     * @author bai
     */
    List<UserFileTreeDTO> getUserFileTreeListByIds(List<Integer> ids, Integer userId, UserFileStatusEnum userFileStatus);

    /**
     * 获取子文件树
     *
     * @param userFileTreeList 文件集合集合
     * @param userId           用户ID
     * @author bai
     */
    void getSubUserFileTree(Integer userId, List<UserFileTreeDTO> userFileTreeList);

    /**
     * 获取文件树占用空间
     *
     * @param userFileTreeDTOList 文件或文件夹对象集合
     * @author bai
     */
    Long getUserFileTreeSpaceUsage(List<UserFileTreeDTO> userFileTreeDTOList);

    /**
     * 复制
     *
     * @param targetId                  目标文件夹ID
     * @param userId                    用户ID
     * @param sourceUserFileTreeDTOList 源文件或文件夹对象集合
     * @param totalSize                 总占用空间
     */
    void copy(Integer targetId, Integer userId, List<UserFileTreeDTO> sourceUserFileTreeDTOList, Long totalSize);

    /**
     * 删除文件或文件夹
     *
     * @param delUserFileTreeList 要删除的文件或文件夹ID集合
     * @param userId              用户ID
     */
    void delete(List<UserFileTreeDTO> delUserFileTreeList, Integer userId);

    /**
     * 修改文件状态
     *
     * @param ids            文件或文件夹ID集合
     * @param userId         用户ID
     * @param userFileStatus 文件状态
     */
    void updateUserFileStatusById(List<Integer> ids, Integer userId, UserFileStatusEnum userFileStatus);


    /**
     * 获取文件及文件夹列表
     *
     * @param userId         用户ID
     * @param parentId       用户文件信息
     * @param userFileStatus 用户文件信息
     * @return 获取到的用户文件数据
     * @author bai
     */
    List<UserFileDO> getUserFileListByUserIdAndParentId(Integer userId, Integer parentId, Integer userFileStatus);

    /**
     * 根据path与itemType获取父文件数据
     *
     * @param userId 用户ID
     * @param path   所属文件夹路径
     * @return 文件夹数据
     * @author bai
     */
    UserFileDO getParentUserFileByPathAndItemType(Integer userId, String path);

    /**
     * 根据文件ID获取文件数据
     *
     * @param id             文件或文件夹ID
     * @param userId         用户ID
     * @param userFileStatus 文件状态 1 正常 -1 删除 null 全部
     * @return 文件夹数据
     * @author bai
     */
    UserFileDO getUserFileById(Integer id, Integer userId, Integer userFileStatus);

    /**
     * 根据文件ID集合获取文件数据
     *
     * @param ids            文件或文件夹ID集合
     * @param userId         用户ID
     * @param userFileStatus 文件状态 1 正常 -1 删除 null 全部
     * @return 文件夹数据
     * @author bai
     */
    List<UserFileTreeDTO> getUserFileTreeDTOByIdsAndUserId(List<Integer> ids, Integer userId, Integer userFileStatus);

    /**
     * 根据文件ID集合递归获取文件数据
     *
     * @param sourceUserFileTreeDTOList 源文件或文件夹对象集合
     * @param userId                    用户ID
     * @author bai
     */
    void recursiveGetUserFileTreeDTO(List<UserFileTreeDTO> sourceUserFileTreeDTOList, Integer userId);

    /**
     * 根据文件路径模糊查询文件数据
     *
     * @param filePath       文件或文件夹路径
     * @param userId         用户ID
     * @param userFileStatus 文件状态 1 正常 -1 删除
     * @return 文件夹数据
     * @author bai
     */
    List<UserFileDO> getUserFileByLikeFilePath(String filePath, Long userId, Integer userFileStatus);

    /**
     * 修改文件路径
     *
     * @param parentId           父文件夹ID
     * @param userId             用户ID
     * @param newPath            新路径
     * @param userFileStatusEnum 文件状态
     */
//    Integer updateFilePathByParentId(Integer parentId, Integer userId, String newPath, UserFileStatusEnum userFileStatusEnum);

    /**
     * 下载
     *
     * @param userFileTreeDTOList 文件或文件夹信息集合
     * @param userId              用户ID
     * @return DownloadedFile对象
     */
    DownloadedFile download(List<UserFileTreeDTO> userFileTreeDTOList, Integer userId) throws IOException;
}
