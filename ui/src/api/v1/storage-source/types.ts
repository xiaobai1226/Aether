/**
 * 存储源类型定义
 */

/**
 * 存储源类型枚举
 */
export enum StorageSourceType {
  /** 本地存储 */
  LOCAL = 0
}

/**
 * 存储源状态枚举
 */
export enum StorageSourceStatus {
  /** 禁用 */
  DISABLED = 0,
  /** 启用 */
  ENABLED = 1
}

/**
 * 存储源信息
 */
export interface StorageSourceDTO {
  /** 主键ID */
  id: number
  
  /** 用户ID */
  userId: number
  
  /** 存储源名称 */
  name: string
  
  /** 存储源类型 */
  type: StorageSourceType
  
  /** 存储路径 */
  path: string
  
  /** 是否为默认存储源 */
  isDefault: number
  
  /** 状态 */
  status: StorageSourceStatus
  
  /** 创建时间 */
  createTime: string
  
  /** 修改时间 */
  updateTime: string
}

/**
 * 添加存储源请求
 */
export interface AddStorageSourceRequest {
  /** 存储源名称 */
  name: string
  
  /** 存储源类型 */
  type: StorageSourceType
  
  /** 存储路径 */
  path: string
  
  /** 是否设为默认 */
  isDefault?: number
}

/**
 * 更新存储源请求
 */
export interface UpdateStorageSourceRequest {
  /** 存储源ID */
  id: number
  
  /** 存储源名称 */
  name: string
}

/**
 * 设置文件夹存储源请求
 */
export interface SetFolderStorageSourceRequest {
  /** 文件夹ID */
  folderId: number
  
  /** 新的存储源ID */
  storageSourceId: number
}

