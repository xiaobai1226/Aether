package com.xiaobai1226.aether.core.service.intf;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaobai1226.aether.core.domain.dto.*;
import com.xiaobai1226.aether.core.domain.vo.UserFileVO;
import com.xiaobai1226.aether.core.domain.vo.UserFolderVO;
import com.xiaobai1226.aether.core.enums.UserFileItemTypeEnum;
import com.xiaobai1226.aether.core.enums.UserFileStatusEnum;
import com.xiaobai1226.aether.dao.domain.dto.PageResult;
import com.xiaobai1226.aether.dao.domain.dto.UserFileDTO;
import com.xiaobai1226.aether.dao.domain.dto.UserFileTreeDTO;
import com.xiaobai1226.aether.dao.domain.entity.UserFileDO;

import org.noear.solon.core.handle.DownloadedFile;

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
        UserFileDO getParentFolderByPath(final Long userId, Long parentId, String path);

        /**
         * 根据path获取文件夹DTO（包含存储源信息）
         * path为空时返回根目录
         *
         * @param userId 用户ID
         * @param path   文件夹路径
         * @return 文件夹DTO
         * @author bai
         */
        UserFolderDTO getFolderDTO(final Long userId, String path);

        /**
         * 根据path获取父文件夹ID，如果不存在则创建这个文件夹
         *
         * @param userId         用户ID
         * @param parentFolder   父文件夹对象
         * @param path           所属文件夹路径
         * @return 文件夹对象
         * @author bai
         */
        UserFileDO getParentFolderByPathOrCreate(final Long userId, UserFolderDTO parentFolder, String path);

        /**
         * 根据path获取用户文件数据
         *
         * @param userId 用户ID
         * @param path   所属文件夹路径
         * @return 文件夹数据
         * @author bai
         */
        UserFileDTO getUserFileDTOByPath(final Long userId, String path);

        /**
         * 获取文件及文件夹列表
         *
         * @param userId     用户ID
         * @param parentId   父ID
         * @param userFileVO 用户文件信息
         * @return 获取到的用户文件数据
         * @author bai
         */
        PageResult<UserFileDTO> getFileList(final Long userId, Long parentId, UserFileVO userFileVO);

        /**
         * 根据文件名称获取文件数据
         *
         * @param fileName       文件或文件夹名称
         * @param parentId       父ID
         * @param userId         用户ID
         * @param userFileStatus 文件状态
         *                       // * @param itemType 文件类型
         * @return 文件夹数据
         * @author bai
         */
        // UserFileDO getUserFileByName(String fileName, Long userId, Long parentId,
        // UserFileStatusEnum userFileStatus, UserFileItemTypeEnum itemType);
        UserFileDO getUserFileByName(String fileName, final Long userId, Long parentId,
                        UserFileStatusEnum userFileStatus);

        /**
         * 新建文件夹
         *
         * @param folderName       文件夹名称
         * @param parentUserFileDO 父文件夹数据
         * @param userId           用户ID
         * @return 文件夹对象
         * @author bai
         */
        UserFileDO newFolder(String folderName, UserFolderDTO parentFolder, final Long userId);

        /**
         * 根据父目录获取存储源ID
         *
         * @param parentUserFile 父文件夹对象
         * @param userId         用户ID
         * @return 存储源ID
         * @author bai
         */
        Long getStorageSourceIdByParent(UserFileDO parentUserFile, Long userId);

        /**
         * 根据文件ID获取文件数据
         *
         * @param id             文件或文件夹ID
         * @param userId         用户ID
         * @param userFileStatus 文件状态 null 全部
         * @return 文件夹数据
         * @author bai
         */
        UserFileDO getUserFileByIdAndUserId(Long id, final Long userId, UserFileStatusEnum userFileStatus);

        /**
         * 重命名
         *
         * @param id             文件或文件夹ID
         * @param userId         用户ID
         * @param newName        新名称
         * @param userFileStatus 文件状态
         * @return 修改结果
         */
        Boolean updateFileNameById(Long id, final Long userId, String newName, UserFileStatusEnum userFileStatus);

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
        Boolean rename(Long id, final Long userId, String newName, UserFileDO userFileDO,
                        UserFileStatusEnum userFileStatus);

        /**
         * 获取文件夹列表
         *
         * @param userId       用户ID
         * @param parentId     父ID
         * @param userFolderVO 用户文件夹信息
         * @return 查询到结果
         * @author bai
         */
        PageResult<UserFileDO> getFolderList(final Long userId, Long parentId, UserFolderVO userFolderVO);

        /**
         * 根据文件ID集合获取文件数据
         *
         * @param ids            文件或文件夹ID集合
         * @param userId         用户ID
         * @param userFileStatus 文件状态
         * @return 文件夹数据
         * @author bai
         */
        List<UserFileDO> getUserFileByIdsAndUserId(List<Long> ids, final Long userId,
                        UserFileStatusEnum userFileStatus);

        /**
         * 获取所有子文件夹（包括子文件夹的子文件夹）
         *
         * @param userId 用户ID
         * @param ids    文件或文件夹ID集合
         */
        List<Long> getAllSubfolders(final Long userId, List<Long> ids);

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
        Long getCountByNames(List<String> fileNames, final Long userId, Long parentId,
                        UserFileStatusEnum userFileStatus,
                        UserFileItemTypeEnum itemType);

        /**
         * 根据id集合修改parentId
         *
         * @param sourceIds      源文件或文件夹ID集合
         * @param targetId       目标文件夹ID
         * @param userId         用户ID
         * @param userFileStatus 文件状态
         */
        void updateParentIdByIds(List<Long> sourceIds, Long targetId, final Long userId,
                        UserFileStatusEnum userFileStatus);

        /**
         * 根据文件ID集合获取文件集合
         *
         * @param ids            文件或文件夹ID集合
         * @param userId         用户ID
         * @param userFileStatus 文件状态
         * @return 文件夹数据
         * @author bai
         */
        List<UserFileDTO> getUserFileDTOListByIds(List<Long> ids, final Long userId, UserFileStatusEnum userFileStatus);

        /**
         * 根据文件ID集合获取文件树集合
         *
         * @param ids            文件或文件夹ID集合
         * @param userId         用户ID
         * @param userFileStatus 文件状态
         * @return 文件夹数据
         * @author bai
         */
        List<UserFileTreeDTO> getUserFileTreeListByIds(List<Long> ids, final Long userId,
                        UserFileStatusEnum userFileStatus);

        /**
         * 获取子文件树
         *
         * @param userFileTreeList 文件集合集合
         * @param userId           用户ID
         * @author bai
         */
        void getSubUserFileTree(final Long userId, List<UserFileTreeDTO> userFileTreeList);

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
         * @param targetFolder              目标文件夹
         * @param userId                    用户ID
         * @param sourceUserFileTreeDTOList 源文件或文件夹对象集合
         * @param totalSize                 总占用空间
         */
        void copy(UserFolderDTO targetFolder, final Long userId, List<UserFileTreeDTO> sourceUserFileTreeDTOList, Long totalSize);

        /**
         * 删除文件或文件夹
         *
         * @param delUserFileTreeList 要删除的文件或文件夹ID集合
         * @param userId              用户ID
         */
        void delete(List<UserFileTreeDTO> delUserFileTreeList, final Long userId);

        /**
         * 修改文件状态
         *
         * @param ids            文件或文件夹ID集合
         * @param userId         用户ID
         * @param userFileStatus 文件状态
         */
        void updateUserFileStatusById(List<Long> ids, final Long userId, UserFileStatusEnum userFileStatus);

        /**
         * 下载
         *
         * @param userFileTreeDTOList 文件或文件夹信息集合
         * @param userId              用户ID
         * @return DownloadedFile对象
         */
        DownloadedFile download(List<UserFileTreeDTO> userFileTreeDTOList, final Long userId) throws IOException;
}