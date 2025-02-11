/**
 * 定义枚举属性
 */
export interface SortingMethod {
  // ID
  readonly id: number;
  // 名称
  readonly name: string;
  // 是否展示
  readonly isShow: boolean;
  // el-table的顺序字符串标识
  readonly elStr: string | null;
}

interface SortingMethodEnumType {
  [key: string]: SortingMethod;
}

/**
 * 创建一个对象来模拟排序方式枚举
 */
export const SortingMethodEnum: SortingMethodEnumType = {
  ASC: { id: 0, name: '升序', isShow: true, elStr: 'ascending' } as const,
  DESC: { id: 1, name: '降序', isShow: true, elStr: 'descending' } as const,
  NONE: { id: 2, name: '无', isShow: false, elStr: null } as const
} as const

export const { ASC, DESC, NONE } = SortingMethodEnum