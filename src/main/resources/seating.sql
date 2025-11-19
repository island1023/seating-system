-- 1. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS seating CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 切换到新创建的数据库
USE seating;

-- 2. 用户表 (User)
-- 用于存储老师的登录信息
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（建议存储加密后的结果）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'TEACHER' COMMENT '用户角色（如 TEACHER, ADMIN）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '系统用户表（教师）';

-- 3. 班级表 (Classroom)
-- 存储班级信息，与老师关联
CREATE TABLE classroom (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    teacher_id BIGINT NOT NULL COMMENT '关联的教师ID',
    name VARCHAR(100) NOT NULL COMMENT '班级名称',
    description VARCHAR(255) COMMENT '班级描述',
    -- seat_layout 存储 JSON 字符串，定义班级的座位布局（如行数、列数、讲台位置等）
    seat_layout TEXT COMMENT '座位布局配置（JSON格式）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES user(id)
) COMMENT '班级信息表';

-- 4. 学生表 (Student)
-- 存储学生基本信息及自定义信息
CREATE TABLE student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    class_id BIGINT NOT NULL COMMENT '所属班级ID',
    student_no VARCHAR(20) NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别',
    -- custom_info 存储自定义信息，如爱好、特殊需求等，JSON 格式
    custom_info TEXT COMMENT '学生自定义信息（JSON格式）',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否活跃（是否在当前班级中）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classroom(id),
    UNIQUE KEY uk_class_student (class_id, student_no) -- 同一班级学号唯一
) COMMENT '学生信息表';

-- 5. 排座记录表 (SeatingRecord)
-- 存储每次排座的最终结果快照
CREATE TABLE seating_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    class_id BIGINT NOT NULL COMMENT '关联的班级ID',
    record_name VARCHAR(100) NOT NULL COMMENT '排座记录名称（如 2025年秋季-第一次随机排座）',
    -- layout_snapshot 存储 JSON 字符串，记录当时的座位图：{row: 1, col: 2, studentId: 101, studentName: '张三'}
    layout_snapshot LONGTEXT NOT NULL COMMENT '座位布局快照（JSON格式，记录每个座位上的学生ID）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    FOREIGN KEY (class_id) REFERENCES classroom(id)
) COMMENT '排座历史记录表';

-- 6. 学生分组表 (StudentGroup)
-- 存储老师对学生的分组信息，用于排座时的约束
CREATE TABLE student_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    class_id BIGINT NOT NULL COMMENT '关联的班级ID',
    name VARCHAR(100) NOT NULL COMMENT '分组名称',
    -- student_ids 存储逗号分隔的学生ID列表，或 JSON 数组
    student_ids TEXT COMMENT '该组包含的学生ID列表（逗号分隔或JSON数组）',
    description VARCHAR(255) COMMENT '分组描述（如：需要在一起的、需要分开的等）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classroom(id)
) COMMENT '学生分组信息表';

-- 7. 初始数据插入（可选）：创建一个默认的管理员用户
INSERT INTO user (username, password, real_name, role) VALUES 
('admin', '$2a$10$wTf2z/JcWp.7pX/Z8uS3X.fO0t0.hQ6/jYQp/v.kZ5.tXk5N2p.M', '系统管理员', 'ADMIN');
-- 注意：上面的密码字段值是 'password' 经过 BCrypt 加密后的示例（如果使用 Spring Security），
-- 在你的实际开发中，请确保使用正确的密码加密方式。