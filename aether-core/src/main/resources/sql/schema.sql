/***********************************************************************************************************************
 * File Description:
 *  Netdisk数据库表结构
 *  部分表数据结构
 *  网盘库：存放网盘相关数据
 *  包含表：
 *          user      用户表
 *          file      文件表
 **********************************************************************************************************************/


-- ----------------------------
-- Table structure for
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS user;
CREATE TABLE user
(
    id              int unsigned AUTO_INCREMENT COMMENT '用户ID' PRIMARY KEY,
    username        varchar(15)                          NOT NULL COMMENT '用户名',
    nickname        varchar(20)                          NULL COMMENT '昵称',
#     email           varchar(150)                         NULL COMMENT '邮箱',
#     avatar          varchar(150)                         NULL COMMENT '头像',
    user_password   varchar(70)                          NULL COMMENT '密码',
#     role_id         int unsigned                         NULL COMMENT '角色ID',
    create_time     datetime   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time     datetime   DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    last_login_time datetime                             NULL COMMENT '最近一次登录时间',
    user_status     tinyint(1) DEFAULT 1                 NOT NULL COMMENT '用户状态 0 : 禁用；1 ：启用',
    used_storage    bigint     DEFAULT 0                 NULL COMMENT '已使用存储空间 单位 byte',
    total_storage   bigint                               NULL COMMENT '总存储空间 单位 byte',
    constraint user_info_pk unique (username)
#     constraint user_info_pk2 unique (email)
) COMMENT '用户表' ENGINE = InnoDB
                   CHARACTER SET = utf8;

-- ----------------------------
-- Table structure for
-- 文件表
-- ----------------------------
DROP TABLE IF EXISTS file;
CREATE TABLE file
(
    id          int unsigned AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
    name        varchar(200)                           NOT NULL COMMENT '文件名称',
    path        varchar(200)                           NOT NULL COMMENT '文件路径',
    size        bigint unsigned                        NOT NULL COMMENT '文件大小',
    thumbnail   varchar(100)                           NULL COMMENT '缩略图 只有视频与图片存在',
#     file_status tinyint(1)                             NOT NULL COMMENT '文件状态 0 转码中 1 转码成功 2 转码失败',
    file_type   int unsigned DEFAULT 0                 NULL COMMENT '文件类型 0 其他 1 视频 2音频 3 图片 4 pdf 5 doc 6 excel 7 txt 8 code 9 zip',
    identifier  varchar(50)                            NOT NULL COMMENT 'md5唯一标识',
#     storage_source_id int unsigned                           NOT NULL COMMENT '存储源ID 1 本地存储',
    create_time datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    constraint file_pk2 unique (name)
) COMMENT '文件表' ENGINE = InnoDB
                   CHARACTER SET = utf8;

-- ----------------------------
-- Table structure for
-- 用户文件表
-- ----------------------------
DROP TABLE IF EXISTS user_file;
CREATE TABLE user_file
(
    id          int unsigned auto_increment COMMENT '主键ID' PRIMARY KEY,
    parent_id   int unsigned DEFAULT 0                 NOT NULL COMMENT '父目录ID，0为根目录',
    file_id     int unsigned                           NULL COMMENT '文件ID，NULL为空文件或文件夹',
    user_id     int unsigned                           NOT NULL COMMENT '所属用户ID',
    item_type   tinyint(1)                             NOT NULL COMMENT '类型 0 目录 1 文件',
    file_status tinyint(1)   DEFAULT 1                 NOT NULL COMMENT '文件状态 1 正常 0 回收站',
    name        varchar(300)                           NOT NULL COMMENT '文件夹或文件名称',
    category    int unsigned DEFAULT NULL              NULL COMMENT '文件分类 0 其他 1 视频 2音频 3 图片 4 文档 文件夹为null',
#     storage_source_id int unsigned                         NOT NULL COMMENT '存储源ID 1 本地存储',
    create_time datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'
) COMMENT '用户文件表' ENGINE = InnoDB
                       CHARACTER SET = utf8;

-- ----------------------------
-- Table structure for
-- 回收站表
-- ----------------------------
DROP TABLE IF EXISTS recycle_bin;
CREATE TABLE recycle_bin
(
    id           int unsigned auto_increment COMMENT '主键ID' PRIMARY KEY,
    recycle_id   varchar(50)                        NOT NULL COMMENT '回收ID',
    is_root      tinyint(1) unsigned                NOT NULL COMMENT '是否为顶层目录 0 否 1 是',
    user_file_id int unsigned                       NOT NULL COMMENT '用户文件ID',
    user_id      int unsigned                       NOT NULL COMMENT '所属用户ID',
    create_time  datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间'
) COMMENT '回收站表' ENGINE = InnoDB
                     CHARACTER SET = utf8;

-- ----------------------------
-- Table structure for
-- 分享表
-- ----------------------------
DROP TABLE IF EXISTS share;
CREATE TABLE share
(
    id              varchar(50) COMMENT '主键ID' PRIMARY KEY,
    extraction_code varchar(4)                             NOT NULL COMMENT '提取码，4位字符串，只包含字母数字',
    view_num        int unsigned DEFAULT 0                 NOT NULL COMMENT '浏览次数',
    download_num    int unsigned DEFAULT 0                 NOT NULL COMMENT '下载次数',
    save_num        int unsigned DEFAULT 0                 NOT NULL COMMENT '保存次数',
    validity_period int                                    NOT NULL COMMENT '有效期，单位 天，0为永久',
    user_id         int unsigned                           NOT NULL COMMENT '所属用户ID',
    create_time     datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间'
) COMMENT '分享文件表' ENGINE = InnoDB
                       CHARACTER SET = utf8;

-- ----------------------------
-- Table structure for
-- 分享文件中间表
-- ----------------------------
DROP TABLE IF EXISTS share_user_file;
CREATE TABLE share_user_file
(
    id           int unsigned auto_increment COMMENT '主键ID' PRIMARY KEY,
    share_id     varchar(50)  NOT NULL COMMENT '分享ID',
    user_file_id int unsigned NOT NULL COMMENT '用户文件ID'
) COMMENT '分享文件中间表' ENGINE = InnoDB
                           CHARACTER SET = utf8;