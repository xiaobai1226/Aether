package com.xiaobai1226.aether.common.enums;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 分类枚举
 *
 * @author bai
 */
@AllArgsConstructor
public enum CategoryEnum {

    /**
     * 其他
     */
    OTHER(0, "其它", Set.of()),

    /**
     * 视频
     */
    VIDEO(1, "视频", Set.of("mp4", "avi", "rmvb", "mkv", "mov")),

    /**
     * 音频
     */
    AUDIO(2, "音频", Set.of("mp3", "wav", "wma", "mp2", "flac", "midi", "ra", "ape", "aac", "cda")),

    /**
     * 图片
     */
    PICTURE(3, "图片", Set.of("jpeg", "jpg", "png", "gif", "bmp", "dds", "psd", "pdt", "webp", "xmp", "svg", "tiff", "heic")),

    /**
     * 文档
     */
    DOC(4, "文档", Set.of("pdf", "doc", "docx", "odt", "rtf", "xls", "xlsx", "txt", "ppt", "pptx", "csv", "h", "c", "hpp", "hxx", "cpp", "cc", "c++", "cxx", "m", "o", "s", "dll", "cs", "java", "class", "js", "ts", "css", "scss", "vue", "jsx", "sql", "md", "json", "html", "xml", "vsd", "chm", "epub", "mobi"));

    /**
     * id
     */
    private final Integer id;

    /**
     * name
     */
    private final String name;

    /**
     * 后缀Set
     */
    private final Set<String> suffixSet;

    /**
     * 获取id
     *
     * @return id
     */
    public Integer id() {
        return this.id;
    }

    /**
     * 是否是图片
     */
    public static Boolean isPictureBySuffix(String suffix) {
        return PICTURE.suffixSet.contains(suffix);
    }

    /**
     * 是否是图片
     */
    public static Boolean isPictureByName(String name) {
        var ext = FileNameUtil.extName(name);
        return StrUtil.isNotEmpty(ext) && PICTURE.suffixSet.contains(ext.toLowerCase());
    }

    /**
     * 是否是视频
     */
    public static Boolean isVideoBySuffix(String suffix) {
        return VIDEO.suffixSet.contains(suffix);
    }

    /**
     * 是否是视频
     */
    public static Boolean isVideoByName(String name) {
        var ext = FileNameUtil.extName(name);
        return StrUtil.isNotEmpty(ext) && VIDEO.suffixSet.contains(ext.toLowerCase());
    }

    /**
     * 根据id获取后缀集合
     */
    public static Set<String> getSuffixSet(Integer id) {
        for (var value : values()) {
            if (Objects.equals(value.id, id)) {
                return value.suffixSet;
            }
        }

        return Stream.of(VIDEO.suffixSet, AUDIO.suffixSet, PICTURE.suffixSet, DOC.suffixSet).flatMap(Set::stream).collect(Collectors.toSet());
    }

    /**
     * 获取所有后缀
     */
    public static Set<String> getAllSuffix() {
        return Stream.of(VIDEO.suffixSet, AUDIO.suffixSet, PICTURE.suffixSet, DOC.suffixSet).flatMap(Set::stream).collect(Collectors.toSet());
    }

    /**
     * 根据文件名称返回枚举
     *
     * @return 匹配到的枚举
     */
    public static CategoryEnum getEnumByFileName(String name) {
        var ext = FileNameUtil.extName(name);
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
    public static CategoryEnum getEnumByExt(String ext) {
        for (var value : values()) {
            if (value.suffixSet.contains(ext.toLowerCase())) {
                return value;
            }
        }
        return OTHER;
    }
}
