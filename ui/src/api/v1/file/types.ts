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

  /**
   * 排序字段
   */
  sortingField: number;

  /**
   * 排序方式
   */
  sortingMethod: number;
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
   * 文件后缀
   */
  suffix?: string;

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
   * md5唯一标识
   */
  identifier?: string;

  /**
   * 存储源ID
   */
  storageSourceId?: number;
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

export interface UploadInitRequest {
  taskId?: string;
  path?: string;
  relativePath?: string;
  fileName: string;
  fileSize: number;
  identifier: string;
  totalChunks: number;
}

export interface UploadInitResponse {
  taskId: string;
  status: number;
  chunkSize: number;
  totalChunks: number;
  uploadedChunks: number[];
  uploadedSize: number;
}

export interface UploadChunkRequest {
  taskId: string;
  fileName: string;
  fileSize: number;
  identifier: string;
  chunkIndex: number;
  totalChunks: number;
}

export interface UploadChunkResponse {
  taskId: string;
  status: number;
  chunkIndex: number;
  uploadedSize: number;
  receivedChunks: number;
}

export interface UploadStatusRequest {
  taskId: string;
}

export interface UploadStatusResponse {
  taskId: string;
  status: number;
  totalChunks: number;
  uploadedChunks: number[];
  uploadedSize: number;
}

export interface UploadCompleteRequest {
  taskId: string;
  path?: string;
  fileName: string;
  fileSize: number;
  identifier: string;
  totalChunks: number;
}

export interface UploadCompleteResponse {
  taskId: string;
  status: number;
}

export interface UploadCancelRequest {
  taskId: string;
}

/**
 * 移动请求信息
 */
export interface MoveRequest {
  /**
   * 主键ID集合
   */
  sourceIds: number[];

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
   * 主键ID集合
   */
  sourceIds: number[];

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
   * 主键ID集合
   */
  ids: number[];
}

/**
 * 下载任务状态响应
 */
export interface DownloadTaskStatusResponse {
  /**
   * 任务ID
   */
  taskId: string;

  /**
   * 任务状态：0 排队中 1 执行中 2 成功 3 失败 4 过期
   */
  status: number;

  /**
   * 进度（0-100）
   */
  progress: number;

  /**
   * 下载文件名
   */
  fileName?: string;

  /**
   * 任务下载签名
   */
  downloadSign?: string;

  /**
   * 需要打包的文件总数
   */
  totalFileCount?: number;

  /**
   * 已打包文件数
   */
  completedFileCount?: number;

  /**
   * 错误信息
   */
  errorMsg?: string;
}

/**
 * 创建下载响应
 */
export interface DownloadCreateResponse {
  /**
   * 下载类型：DIRECT 直接下载，TASK 任务下载
   */
  type: 'DIRECT' | 'TASK';

  /**
   * 直接下载签名
   */
  sign?: string;

  /**
   * 下载任务ID
   */
  taskId?: string;
}

/**
 * 创建文件直链请求
 */
export interface CreateDirectLinkRequest {
  /**
   * 用户文件ID
   */
  id: number;

  /**
   * 直链有效期（天），0为永久
   */
  expireDays?: number;
}

/**
 * 创建文件直链响应
 */
export interface CreateDirectLinkResponse {
  /**
   * 直链token
   */
  token: string;

  /**
   * 过期时间，NULL为永久
   */
  expireAt?: string;
}

/**
 * 撤销文件直链请求
 */
export interface RevokeDirectLinkRequest {
  /**
   * 直链token
   */
  token: string;
}

/**
 * 获取直链记录分页请求
 */
export interface GetDirectLinkListByPageRequest {
  /**
   * 页码
   */
  pageNum: number;

  /**
   * 每页条数
   */
  pageSize: number;
}

/**
 * 直链记录
 */
export interface DirectLinkRecord {
  token: string;
  userFileId: number;
  fileName: string;
  folderPath?: string;
  suffix?: string;
  status: number;
  expireAt?: string;
  createTime: string;
  bizStatus: 'ACTIVE' | 'EXPIRED' | 'DISABLED';
}

/**
 * 获取直链记录分页响应
 */
export interface GetDirectLinkListByPageResponse {
  pageNum: number;
  pageSize: number;
  total: number;
  totalPage: number;
  list: Array<DirectLinkRecord>;
}

/**
 * 更新直链有效期请求
 */
export interface UpdateDirectLinkExpireRequest {
  token: string;
  expireDays: number;
}