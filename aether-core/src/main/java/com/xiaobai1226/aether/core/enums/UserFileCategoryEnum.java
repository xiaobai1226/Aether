//package com.xiaobai1226.aether.core.enums;
//
//import java.util.Objects;
//
///**
// * 文件分类枚举
// *
// * @author bai
// */
//public enum UserFileCategoryEnum {
//
//    /**
//     * 其他
//     */
//    OTHER(0),
//
//    /**
//     * 视频
//     */
//    VIDEO(1),
//
//    /**
//     * 音频
//     */
//    AUDIO(2),
//
//    /**
//     * 图片
//     */
//    PICTURE(3),
//
//    /**
//     * 文档
//     */
//    DOCUMENT(4),
//
//    /**
//     * 压缩包
//     */
//    ARCHIVE(5);
//
//    /**
//     * flag成员变量
//     */
//    private final Integer flag;
//
//    /**
//     * 构造方法
//     *
//     * @param flag id
//     */
//    UserFileCategoryEnum(Integer flag) {
//        this.flag = flag;
//    }
//
//    /**
//     * 获取flag
//     *
//     * @return flag
//     */
//    public Integer flag() {
//        return this.flag;
//    }
//
//    /**
//     * 根据文件名称返回枚举
//     *
//     * @return 匹配到的枚举
//     */
//    public static UserFileCategoryEnum getEnumByFlag(Integer flag) {
//        for (var value : values()) {
//            if (Objects.equals(value.flag, flag)) {
//                return value;
//            }
//        }
//        return OTHER;
//    }
//
//    /**
//     * 判断是否是图片
//     *
//     * @return 结果
//     */
//    public static Boolean isPicture(Integer flag) {
//        return Objects.equals(PICTURE.flag, flag);
//    }
//
//    /**
//     * 判断是否是视频
//     *
//     * @return 结果
//     */
//    public static Boolean isVideo(Integer flag) {
//        return Objects.equals(VIDEO.flag, flag);
//    }
//}
