/**
 * 获取用户文件请求信息
 */
export interface GetFileListByPageRequest {
  /**
   * 分类ID
   */
  category: number | null;

  /**
   * 文件夹路径
   */
  path?: string;

  /**
   * 页码
   */
  pageNum: number;

  /**
   * 每页元素数
   */
  pageSize: number;
}

/**
 * 获取用户文件夹请求信息
 */
export interface GetFolderListByPageRequest {
  /**
   * 文件夹路径
   */
  path?: string;

  /**
   * 页码
   */
  pageNum: number;

  /**
   * 每页元素数
   */
  pageSize: number;
}

/**
 * 用户文件响应信息
 */
export interface GetFileListByPageResponse {
  /**
   * 页码
   */
  pageNum: number;

  /**
   * 每页条数
   */
  pageSize: number;

  /**
   * 总条数
   */
  total: number;

  /**
   * 总页数
   */
  totalPage: number;

  /**
   * 分页数据
   */
  list: Array<UserFileInfo>;
}

/**
 * 用户文件信息
 */
export interface UserFileInfo {
  /**
   * 主键ID
   */
  id?: number;

  /**
   * 父目录ID，0为根目录
   */
  parentId?: number;

  /**
   * 文件夹路径
   */
  path: string;

  /**
   * 文件ID，NULL为空文件，文件夹此列一定为空
   */
  fileId?: number;

  /**
   * 所属用户ID
   */
  userId?: number;

  /**
   * 文件类型 0 目录 1 文件
   */
  itemType: number;

  /**
   * 文件状态 1 正常 0 回收站
   */
  fileStatus?: number;

  /**
   * 文件夹或文件名称
   */
  name?: string;

  /**
   * 文件分类
   */
  category?: number;

  /**
   * 创建时间
   */
  createTime?: string;

  /**
   * 修改时间
   */
  updateTime?: string;

  /**
   * 文件大小
   */
  size?: number;

  /**
   * 缩略图
   */
  thumbnail?: string;

  /**
   * 文件类型
   */
  fileType?: number;

  /**
   * md5唯一标识
   */
  identifier?: string;
}

/**
 * 新建文件夹请求信息
 */
export interface NewFolderRequest {
  /**
   * 所属文件夹路径
   */
  path?: string;

  /**
   * 文件夹名称
   */
  folderName: string;
}

/**
 * 重命名请求信息
 */
export interface FileRenameRequest {
  /**
   * 主键ID
   */
  id: number;

  /**
   * 所属文件夹路径
   */
  // path?: string;

  /**
   * 新名称
   */
  newName: string;

  /**
   * 文件类型 0 目录 1 文件
   */
  // itemType: number;
}

/**
 * 上传文件请求信息
 */
export interface UploadFileRequest {
  /**
   * 任务ID
   */
  taskId: string;

  /**
   * 所属文件夹路径
   */
  path?: string;

  /**
   * 上传文件夹，文件所属文件夹路径
   */
  relativePath?: string;

  /**
   * 文件名称
   */
  fileName: string;

  /**
   * 文件大小
   */
  fileSize: number;

  /**
   * md5码
   */
  identifier: string;

  /**
   * 切片索引
   */
  chunkIndex: number;

  /**
   * 总切片数
   */
  totalChunks: number;
}

/**
 * 上传文件响应信息
 */
export interface UploadFileResponse {

  /**
   * 任务ID
   */
  taskId: string;

  /**
   * 上传状态
   */
  status: number;
}

/**
 * 移动请求信息
 */
export interface MoveRequest {
  /**
   * 主键ID集合用逗号分割的字符串
   */
  sourceIds: string;

  /**
   * 目标文件夹路径
   */
  targetPath?: string;
}

/**
 * 复制请求信息
 */
export interface CopyRequest {
  /**
   * 主键ID集合用逗号分割的字符串
   */
  sourceIds: string;

  /**
   * 目标文件夹路径
   */
  targetPath?: string;
}

/**
 * 删除请求信息
 */
export interface DeleteRequest {
  /**
   * 主键ID集合用逗号分割的字符串
   */
  ids: string;
}