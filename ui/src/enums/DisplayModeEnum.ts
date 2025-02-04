/**
 * 定义枚举属性
 */
export interface DisplayMode {
  readonly id: number;
  readonly name: string;
}

interface DisplayModeEnumType {
  [key: string]: DisplayMode;
}

/**
 * 创建一个对象来模拟显示模式枚举
 */
export const DisplayModeEnum: DisplayModeEnumType = {
  List: { id: 0, name: '列表模式' } as const,
  Thumbnail: { id: 1, name: '缩略模式' } as const,
  Large: { id: 2, name: '大图模式' } as const
} as const

export const { List, Thumbnail, Large } = DisplayModeEnum