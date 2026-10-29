-- ============================================
-- 1. 系统用户表
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    email VARCHAR(100) NULL COMMENT '邮箱',
    phone VARCHAR(20) NULL COMMENT '手机号',
    nickname VARCHAR(50) NULL COMMENT '昵称',
    avatar VARCHAR(255) NULL COMMENT '头像URL',
    gender TINYINT NOT NULL DEFAULT 1 COMMENT '0-女，1-男',
    status tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT NULL COMMENT '创建人',
    create_time DATETIME NULL COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '修改人',
    update_time DATETIME NULL COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';


-- ============================================
-- 2. 文章分类表（单级分类）
-- ============================================
DROP TABLE IF EXISTS blog_category;
CREATE TABLE blog_category (
    id BIGINT AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) NULL COMMENT '分类描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    create_by BIGINT NULL COMMENT '创建人',
    create_time DATETIME NULL COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '修改人',
    update_time DATETIME NULL COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- ============================================
-- 3. 文章标签表
-- ============================================
DROP TABLE IF EXISTS blog_tag;
CREATE TABLE blog_tag (
    id BIGINT AUTO_INCREMENT COMMENT '标签ID',
    name VARCHAR(64) NOT NULL COMMENT '标签名称',
    create_by BIGINT NULL COMMENT '创建人',
    create_time DATETIME NULL COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '修改人',
    update_time DATETIME NULL COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签表';

-- ============================================
-- 4. 文件元数据表
-- ============================================
DROP TABLE IF EXISTS blog_file;
CREATE TABLE blog_file (
    id BIGINT AUTO_INCREMENT COMMENT '文件ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    storage_type VARCHAR(32) NOT NULL DEFAULT 'LOCAL' COMMENT '存储类型：LOCAL 本地，后续可扩展对象存储',
    bucket_name VARCHAR(64) NOT NULL DEFAULT '' COMMENT '对象存储桶名，本地存储为空字符串',
    storage_key VARCHAR(128) NOT NULL COMMENT '本地相对路径或桶内对象键，不包含桶名，不存临时签名URL',
    mime_type VARCHAR(128) NOT NULL COMMENT '文件MIME类型',
    file_size BIGINT UNSIGNED NOT NULL COMMENT '文件大小，单位字节',
    create_by BIGINT NULL COMMENT '创建人',
    create_time DATETIME NULL COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '修改人',
    update_time DATETIME NULL COMMENT '修改时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件元数据表';

-- ============================================
-- 5. 文章表
-- ============================================
DROP TABLE IF EXISTS blog_article;
CREATE TABLE blog_article (
    id BIGINT AUTO_INCREMENT COMMENT '文章ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    summary VARCHAR(500) NOT NULL DEFAULT '' COMMENT '文章摘要',
    content_markdown LONGTEXT NOT NULL COMMENT 'Markdown正文，唯一正文来源',
    author_id BIGINT NOT NULL COMMENT '作者ID，关联 sys_user.id',
    category_id BIGINT NULL COMMENT '分类ID，关联 blog_category.id，NULL 表示未分类',
    cover_file_id BIGINT NULL COMMENT '封面文件ID，关联 blog_file.id',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '文章状态：0-草稿，1-已发布，2-已下架',
    published_at DATETIME NULL COMMENT '首次发布时间，首次发布前为NULL',
    create_by BIGINT NULL COMMENT '创建人',
    create_time DATETIME NULL COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '修改人',
    update_time DATETIME NULL COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_article_author (author_id),
    KEY idx_article_cover (cover_file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- ============================================
-- 6. 文章标签关联表（一篇文章可有多个标签）
-- ============================================
DROP TABLE IF EXISTS blog_article_tag;
CREATE TABLE blog_article_tag (
    article_id BIGINT NOT NULL COMMENT '文章ID，关联 blog_article.id',
    tag_id BIGINT NOT NULL COMMENT '标签ID，关联 blog_tag.id',
    create_by BIGINT NULL COMMENT '创建人',
    create_time DATETIME NULL COMMENT '创建时间',
    PRIMARY KEY (article_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';
