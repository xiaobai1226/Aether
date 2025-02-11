/**
 * 定义枚举属性
 */
export interface SortingField {
  // ID
  readonly id: number;
  // 字段名称
  readonly name: string;
  // el-table对应的字段
  readonly elField: string;
}

interface SortingFieldEnumType {
  [key: string]: SortingField;
}

/**
 * 创建一个对象来模拟排序字段枚举
 */
export const SortingFieldEnum: SortingFieldEnumType = {
  Name: { id: 0, name: '名称', elField: 'fileName' } as const,
  CreateTime: { id: 1, name: '创建时间', elField: 'createTime' } as const,
  UpdateTime: { id: 2, name: '修改时间', elField: 'updateTime' } as const,
  Size: { id: 3, name: '文件大小', elField: 'fileSize' } as const
} as const

export const { Name, CreateTime, UpdateTime, Size } = SortingFieldEnum
