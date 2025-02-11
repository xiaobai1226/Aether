/**
 * 定义枚举属性
 */
export interface Category {
  // 分类ID
  readonly id: number | null;
  // 分类名称
  readonly name: string;
  // 分类图标
  readonly icon: string;
}

interface CategoryEnumType {
  [key: string]: Category;
}

/**
 * 创建一个对象来模拟分类枚举
 */
export const CategoryEnum: CategoryEnumType = {
  All: { id: null, name: '全部', icon: 'all-fill' } as const,
  Video: { id: 1, name: '视频', icon: 'video_fill_light' } as const,
  Audio: { id: 2, name: '音频', icon: 'MusicAcc' } as const,
  Picture: { id: 3, name: '图片', icon: 'image-fill' } as const,
  Doc: { id: 4, name: '文档', icon: 'format-doc' } as const,
  Other: { id: 0, name: '其它', icon: 'more' } as const
} as const

export const { All, Video, Audio, Picture, Doc, Other } = CategoryEnum