/**
 * 获取文件列表请求信息
 */
export interface GetFileListByPageRequest {
  /**
   * 页码
   */
  pageNum: number;

  /**
   * 每页元素数
   */
  pageSize: number;

  /**
   * 排序字段 1 文件名 2 文件修改日期 3 文件大小
   */
  sortField?: number;

  /**
   * 排序顺序 1 升序 2 降序
   */
  sortOrder?: number;
}

/**
 * 文件列表响应信息
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
  list: Array<FileInfo>;
}

/**
 * 文件信息
 */
export interface FileInfo {
  /**
   * 主键ID
   */
  id: number;

  /**
   * 文件名称
   */
  name: string;

  /**
   * 文件存储路径
   */
  path: string;

  /**
   * 文件大小
   */
  size: number;

  /**
   * 缩略图
   */
  thumbnail: string;

  /**
   * md5唯一标识
   */
  identifier: string;

  /**
   * 创建时间
   */
  createTime: string;

  /**
   * 修改时间
   */
  updateTime: string;
}