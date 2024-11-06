package com.xiaobai1226.aether.core.enums;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Set;

/**
 * 文件类型枚举
 *
 * @author bai
 */
public enum FileTypeEnum {

    /**
     * 其他
     */
    OTHER(0, UserFileCategoryEnum.OTHER, "其他", Set.of("")),

    /**
     * 视频
     */
    VIDEO(1, UserFileCategoryEnum.VIDEO, "视频", Set.of("mp4", "avi", "rmvb", "mkv", "mov")),

    /**
     * 音频
     */
    AUDIO(2, UserFileCategoryEnum.AUDIO, "音频", Set.of("mp3", "wav", "wma", "mp2", "flac", "midi", "ra", "ape", "aac", "cda")),

    /**
     * 图片
     */
    PICTURE(3, UserFileCategoryEnum.PICTURE, "图片", Set.of("jpeg", "jpg", "png", "gif", "bmp", "dds", "psd", "pdt", "webp", "xmp", "svg", "tiff")),

    /**
     * pdf
     */
    PDF(4, UserFileCategoryEnum.DOCUMENT, "PDF", Set.of("pdf")),

    /**
     * doc
     */
    DOC(5, UserFileCategoryEnum.DOCUMENT, "Word", Set.of("doc", "docx")),

    /**
     * excel
     */
    EXCEL(6, UserFileCategoryEnum.DOCUMENT, "Excel", Set.of("xls", "xlsx")),

    /**
     * txt
     */
    TXT(7, UserFileCategoryEnum.DOCUMENT, "Txt", Set.of("txt")),

    /**
     * code
     */
    CODE(8, UserFileCategoryEnum.OTHER, "CODE", Set.of("h", "c", "hpp", "hxx", "cpp", "cc", "c++", "cxx", "m", "o", "s", "dll", "cs", "java", "class", "js", "ts", "css", "scss", "vue", "jsx", "sql", "md", "json", "html", "xml")),

    /**
     * 压缩包
     */
    ARCHIVE(9, UserFileCategoryEnum.ARCHIVE, "压缩包", Set.of("rar", "zip", "7z", "cab", "arj", "lzh", "tar", "gz", "ace", "uue", "bz", "jar", "iso", "mpq"));

    /**
     * id
     */
    private final Integer id;

    /**
     * 分类
     */
    private final UserFileCategoryEnum category;

    /**
     * name
     */
    private final String name;

    /**
     * 后缀Set
     */
    private final Set<String> extSet;

    /**
     * 构造方法
     *
     * @param id id
     */
    FileTypeEnum(Integer id, UserFileCategoryEnum category, String name, Set<String> extSet) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.extSet = extSet;
    }

    /**
     * 获取id
     *
     * @return id
     */
    public Integer id() {
        return this.id;
    }

    /**
     * 获取category
     *
     * @return category
     */
    public UserFileCategoryEnum category() {
        return this.category;
    }

    /**
     * 获取extSet
     *
     * @return extSet
     */
    public Set<String> extSet() {
        return this.extSet;
    }

    /**
     * 根据文件名称返回枚举
     *
     * @return 匹配到的枚举
     */
    public static FileTypeEnum getEnumByFileName(String fileName) {
        var ext = FileNameUtil.extName(fileName);
        if (StrUtil.isNotEmpty(ext)) {
            return getEnumByExt(ext);
        }
        return OTHER;
    }

    /**
     * 根据文件后缀返回枚举
     *
     * @return 匹配到的枚举
     */
    public static FileTypeEnum getEnumByExt(String ext) {
        for (var value : values()) {
            if (value.extSet.contains(ext.toLowerCase())) {
                return value;
            }
        }
        return OTHER;
    }
}
