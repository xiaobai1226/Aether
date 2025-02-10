/**
 * 获取URL
 * @param iconName icon名称
 */
const url = (iconName: string): string => {
  return new URL('../assets/icon-image/' + iconName + '.png', import.meta.url).href
}

/**
 * 定义枚举属性
 */
export interface Icon {
  // 图标Url
  readonly iconUrl: string;
  // 后缀集合
  readonly suffixSet: Set<string>;
}

interface IconEnumType {
  [key: string]: Icon;
}

/**
 * 判断是否是图片或视频
 */
export const isImageOrVideo = (suffix: string) => {
  return IMAGE.suffixSet.has(suffix) || VIDEO.suffixSet.has(suffix)
}

/**
 * 创建一个对象来模拟Icon枚举
 */
export const IconEnum: IconEnumType = {
  FOLDER: { iconUrl: url('folder'), suffixSet: new Set([]) } as const,
  FILE: { iconUrl: url('file'), suffixSet: new Set([]) } as const,
  NO_DATA: { iconUrl: url('no_data'), suffixSet: new Set([]) } as const,
  CLEAN: { iconUrl: url('clean'), suffixSet: new Set([]) } as const,
  DELETE: { iconUrl: url('delete'), suffixSet: new Set([]) } as const,
  UPLOAD: { iconUrl: url('upload'), suffixSet: new Set([]) } as const,
  PAUSE: { iconUrl: url('pause'), suffixSet: new Set([]) } as const,
  MUSIC: {
    iconUrl: url('music'),
    suffixSet: new Set(['mp3', 'wav', 'wma', 'mp2', 'flac', 'midi', 'ra', 'ape', 'aac', 'cda'])
  } as const,
  IMAGE: {
    iconUrl: url('image'),
    suffixSet: new Set(['jpeg', 'jpg', 'png', 'gif', 'bmp', 'dds', 'psd', 'pdt', 'webp', 'xmp', 'svg', 'tiff', 'heic', 'ico'])
  } as const,
  VIDEO: { iconUrl: url('video'), suffixSet: new Set(['mp4', 'avi', 'rmvb', 'mkv', 'mov', 'ts']) } as const,
  TXT: {
    iconUrl: url('txt'),
    suffixSet: new Set(['txt'])
  } as const,
  CODE: {
    iconUrl: url('code'),
    suffixSet: new Set(['xml', 'jsp', 'java', 'class', 'dll', 'vue', 'md', 'html', 'sql', 'c', 'h', 'm', 'cpp', 'cc', 'chm'])
  } as const,
  EPUB: { iconUrl: url('epub'), suffixSet: new Set(['epub']) } as const,
  CSS: { iconUrl: url('css'), suffixSet: new Set(['css', 'scss']) } as const,
  JS: { iconUrl: url('js'), suffixSet: new Set(['js', 'json', 'jsx']) } as const,
  PDF: { iconUrl: url('pdf'), suffixSet: new Set(['pdf']) } as const,
  WORD: { iconUrl: url('word'), suffixSet: new Set(['doc', 'docx', 'odt', 'rtf']) } as const,
  EXCEL: { iconUrl: url('excel'), suffixSet: new Set(['xls', 'xlsx', 'csv']) } as const,
  PPT: { iconUrl: url('ppt'), suffixSet: new Set(['ppt', 'pptx']) } as const,
  EXE: { iconUrl: url('exe'), suffixSet: new Set(['exe']) } as const,
  DMG: { iconUrl: url('dmg'), suffixSet: new Set([]) } as const,
  APK: { iconUrl: url('apk'), suffixSet: new Set(['apk']) } as const,
  PS: { iconUrl: url('ps'), suffixSet: new Set([]) } as const,
  VSD: { iconUrl: url('vsd'), suffixSet: new Set(['vsd']) } as const,
  XMIND: { iconUrl: url('xmind'), suffixSet: new Set([]) } as const,
  ZIP: {
    iconUrl: url('zip'),
    suffixSet: new Set(['rar', 'zip', '7z', 'cab', 'arj', 'lzh', 'tar', 'gz', 'ace', 'uue', 'bz', 'jar', 'iso', 'mpq'])
  } as const,
  OTHER: { iconUrl: url('others'), suffixSet: new Set([]) } as const
} as const

export const {
  FOLDER,
  FILE,
  NO_DATA,
  CLEAN,
  DELETE,
  UPLOAD,
  PAUSE,
  CODE,
  CSS,
  DMG,
  EPUB,
  EXCEL,
  EXE,
  IMAGE,
  JS,
  MUSIC,
  OTHER,
  PDF,
  PPT,
  PS,
  TXT,
  VIDEO,
  VSD,
  WORD,
  XMIND,
  ZIP
} = IconEnum