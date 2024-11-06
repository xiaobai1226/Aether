import type { UserFileInfo } from '@/api/file/types'

/**
 * 获取用户文件请求信息
 */
export interface GetRecycleBinListByPageRequest {
  /**
   * 页码
   */
  pageNum: number;

  /**
   * 每页元素数
   */
  pageSize: number;

  /**
   * 排序字段 1 文件名 2 删除时间 3 文件大小 4 有效时间
   */
  sortField?: number;

  /**
   * 排序顺序 1 升序 2 降序
   */
  sortOrder?: number;
}

/**
 * 用户文件响应信息
 */
export interface GetRecycleBinListByPageResponse {
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
  list: Array<RecycleBinFileInfo>;
}

/**
 * 回收站文件信息
 */
export interface RecycleBinFileInfo extends UserFileInfo {

  /**
   * 回收ID
   */
  recycleId: string

  /**
   * 删除时间
   */
  deleteTime?: string;
}

/**
 * 删除或还原请求信息
 */
export interface DeleteOrRestoreRequest {
  /**
   * 回收ID集合用逗号分割的字符串
   */
  recycleIds: string;
}