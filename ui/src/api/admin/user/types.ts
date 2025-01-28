/**
 * 获取用户列表请求信息
 */
export interface GetUserListByPageRequest {
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
 * 用户列表响应信息
 */
export interface GetUserListByPageResponse {
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
  list: Array<UserInfo>;
}

/**
 * 用户信息
 */
export interface UserInfo {
  /**
   * 主键ID
   */
  id: number;

  /**
   * 用户名
   */
  username: string;

  /**
   * 昵称
   */
  nickname: string;

  /**
   * 角色ID 0 普通用户 1 超级管理员
   */
  roleId: number;

  /**
   * 创建时间
   */
  createTime: string;

  /**
   * 修改时间
   */
  updateTime: string;

  /**
   * 最近一次登录时间
   */
  lastLoginTime: string;

  /**
   * 用户状态 0 : 禁用；1 ：启用
   */
  status: number;

  /**
   * 已使用存储空间 单位 byte
   */
  usedStorage: number;

  /**
   * 总存储空间 单位 byte
   */
  totalStorage: number;
}

/**
 * 新增用户请求信息
 */
export interface AddUserRequest {

  /**
   * 用户名
   */
  username: string;

  /**
   * 昵称
   */
  nickname: string;

  /**
   * 密码
   */
  password: string;

  /**
   * 角色ID 0 普通用户 1 超级管理员
   */
  roleId: number;

  /**
   * 用户状态 0 : 禁用；1 ：启用
   */
  status: number;

  /**
   * 总存储空间 单位 byte
   */
  totalStorage: number;
}

/**
 * 修改用户请求信息
 */
export interface UpdateUserRequest {

  /**
   * ID
   */
  id: number;

  /**
   * 用户名
   */
  username?: string;

  /**
   * 昵称
   */
  nickname?: string;

  /**
   * 密码
   */
  password?: string;

  /**
   * 角色ID 0 普通用户 1 超级管理员
   */
  roleId?: number;

  /**
   * 用户状态 0 : 禁用；1 ：启用
   */
  status?: number;

  /**
   * 总存储空间 单位 byte
   */
  totalStorage?: number;
}