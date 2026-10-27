-- ============================================
-- 1. 系统用户表
-- ============================================
drop table if exists sys_user;
CREATE TABLE sys_user (
    id INT AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    email VARCHAR(100) NULL COMMENT '邮箱',
    phone VARCHAR(20) NULL COMMENT '手机号',
    nickname VARCHAR(50) NULL COMMENT '昵称',
    avatar VARCHAR(255) NULL COMMENT '头像URL',
    gender TINYINT NOT NULL DEFAULT 1 COMMENT '0-女，1-男',
    status tinyint not null default 1 COMMENT '状态：0-禁用 1-启用',
    create_by int null COMMENT '创建人',
    create_time datetime null COMMENT '创建时间',
    update_by int null COMMENT '修改人',
    update_time datetime null COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';
