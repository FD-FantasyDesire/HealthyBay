CREATE DATABASE IF NOT EXISTS healthy_bay DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;  
USE healthy_bay;  
CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      username VARCHAR(50) NOT NULL COMMENT '登录账号',
                      student_no VARCHAR(30) COMMENT '学号',
                      password VARCHAR(100) NOT NULL COMMENT '加密密码',
                      real_name VARCHAR(50) COMMENT '真实姓名',
                      gender TINYINT DEFAULT 0 COMMENT '0未知 1男 2女',
                      phone VARCHAR(20),
                      email VARCHAR(100),
                      class_id BIGINT COMMENT '所属班级ID',
                      room_id BIGINT COMMENT '所属房间ID',
                      status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
                      del_flag TINYINT DEFAULT 0,
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      INDEX idx_class_id (class_id),
                      INDEX idx_room_id (room_id),
                      INDEX idx_student_no (student_no)
);

CREATE TABLE role (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      role_name VARCHAR(50),
                      role_key VARCHAR(50),
                      status TINYINT DEFAULT 1,
                      del_flag TINYINT DEFAULT 0,
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE permission (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            perm_name VARCHAR(100),
                            perm_key VARCHAR(100),
                            del_flag TINYINT DEFAULT 0,
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_role (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT,
                           role_id BIGINT,
                           del_flag TINYINT DEFAULT 0,
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                           update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE role_permission (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 role_id BIGINT,
                                 permission_id BIGINT,
                                 del_flag TINYINT DEFAULT 0,
                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE class (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       class_name VARCHAR(100),
                       college VARCHAR(100) COMMENT '所属学院',
                       grade VARCHAR(20),
                       major VARCHAR(100),
                       del_flag TINYINT DEFAULT 0,
                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE manager_class (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               manager_id BIGINT,
                               class_id BIGINT,
                               del_flag TINYINT DEFAULT 0,
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE area (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      area_name VARCHAR(100),
                      area_type TINYINT COMMENT '1区域 2楼栋 3房间',
                      parent_id BIGINT DEFAULT 0,
                      level INT,
                      area_code VARCHAR(50),
                      manager_id BIGINT COMMENT '楼栋/房间管理员ID',
                      del_flag TINYINT DEFAULT 0,
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE infectious_disease (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               disease_name VARCHAR(100) NOT NULL COMMENT '传染病名称',
                               disease_code VARCHAR(50) NOT NULL COMMENT '传染病代码',
                               severity_level TINYINT COMMENT '严重程度 1轻微 2中度 3严重',
                               incubation_period VARCHAR(100) COMMENT '潜伏期',
                               symptoms TEXT COMMENT '常见症状',
                               prevention_measures TEXT COMMENT '预防措施',
                               del_flag TINYINT DEFAULT 0,
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               INDEX idx_disease_code (disease_code)
);

CREATE TABLE health_report (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id BIGINT,
                               class_id BIGINT,
                               room_id BIGINT,
                               temperature DECIMAL(4,1),
                               symptom_json TEXT COMMENT '症状JSON数据',
                               disease_id BIGINT COMMENT '传染病ID，关联infectious_disease表',
                               exposure_history TEXT COMMENT '接触史',
                               vaccination_status TINYINT COMMENT '疫苗接种状态 1已接种 2未接种 3部分接种',
                               risk_level TINYINT COMMENT '1低 2中 3高',
                               report_date DATE,
                               status TINYINT DEFAULT 0 COMMENT '0未处理 1已处理',
                               del_flag TINYINT DEFAULT 0,
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               INDEX idx_user_id (user_id),
                               INDEX idx_class_id (class_id),
                               INDEX idx_disease_id (disease_id),
                               INDEX idx_report_date (report_date),
                               INDEX idx_risk_level (risk_level)
);

CREATE TABLE warning_rule (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              rule_name VARCHAR(100),
                              class_scope BIGINT COMMENT '作用班级ID 可为空表示全局',
                              disease_id BIGINT COMMENT '传染病ID，关联infectious_disease表，可为空表示所有传染病',
                              threshold_count INT,
                              time_window_hours INT,
                              risk_level TINYINT,
                              enabled TINYINT DEFAULT 1,
                              del_flag TINYINT DEFAULT 0,
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                              update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              INDEX idx_disease_id (disease_id),
                              INDEX idx_class_scope (class_scope)
);

CREATE TABLE warning_record (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                rule_id BIGINT,
                                class_id BIGINT,
                                disease_id BIGINT COMMENT '传染病ID，关联infectious_disease表',
                                warning_level TINYINT,
                                trigger_count INT,
                                status TINYINT DEFAULT 0 COMMENT '0未处理 1已处理',
                                del_flag TINYINT DEFAULT 0,
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                INDEX idx_rule_id (rule_id),
                                INDEX idx_class_id (class_id),
                                INDEX idx_disease_id (disease_id),
                                INDEX idx_status (status),
                                INDEX idx_create_time (create_time)
);

CREATE TABLE warning_record_detail (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       warning_record_id BIGINT COMMENT '关联的预警记录ID',
                                       health_report_id BIGINT COMMENT '触发该预警的健康上报ID',
                                       user_id BIGINT COMMENT '触发人的ID',
                                       del_flag TINYINT DEFAULT 0,
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       INDEX idx_warning_record_id (warning_record_id),
                                       INDEX idx_health_report_id (health_report_id),
                                       INDEX idx_user_id (user_id)
);

CREATE TABLE notice (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        title VARCHAR(200),
                        content TEXT,
                        publish_scope TINYINT COMMENT '1全校 2指定班级',
                        class_id BIGINT COMMENT '目标班级ID（针对指定班级发通知）',
                        publisher_id BIGINT,
                        status TINYINT DEFAULT 1,
                        del_flag TINYINT DEFAULT 0,
                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        INDEX idx_publish_scope (publish_scope),
                        INDEX idx_class_id (class_id),
                        INDEX idx_create_time (create_time)
);

CREATE TABLE operation_log (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id BIGINT,
                               operation VARCHAR(200),
                               method VARCHAR(200),
                               request_ip VARCHAR(50),
                               status TINYINT,
                               error_msg TEXT,
                               del_flag TINYINT DEFAULT 0,
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               INDEX idx_user_id (user_id),
                               INDEX idx_operation (operation),
                               INDEX idx_create_time (create_time),
                               INDEX idx_status (status)
);  
-- ----------------------------
-- 预制角色数据
-- 1: 学生, 2: 班级管理员, 3: 系统管理员
-- ----------------------------
INSERT INTO `role` (`id`, `role_name`, `role_key`, `status`) VALUES
(1, '学生', 'student', 1),
(2, '班级管理员', 'class_admin', 1),
(3, '系统管理员', 'system_admin', 1);

-- ----------------------------
-- 预制权限数据
-- ----------------------------
INSERT INTO `permission` (`id`, `perm_name`, `perm_key`) VALUES
(1, '健康上报', 'health:report'),
(2, '查看预警', 'warning:view'),
(3, '发布通知', 'notice:publish'),
(4, '系统设置', 'system:setting'),
(5, '人员管理', 'user:manage'),
(6, '查看通知', 'notice:view'),
(7, '角色权限管理', 'role:manage'),
(8, '班级看板', 'class:dashboard:view'),
(9, '班级管理', 'class:manage'),
(10, '传染病字典', 'disease:manage'),
(11, '预警规则配置', 'rule:manage'),
(12, '查看控制台', 'dashboard:view'),
(13, '审计日志管理', 'log:manage');

-- ----------------------------
-- 角色-权限关联
-- ----------------------------
-- 学生: 健康上报, 查看通知, 查看控制台
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES (1, 1), (1, 6), (1, 12);
-- 班级管理员: 查看预警, 发布通知, 班级看板, 查看控制台
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES (2, 2), (2, 3), (2, 8), (2, 12);
-- 系统管理员: 拥有所有
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES 
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12), (3, 13);

-- ----------------------------
-- 预制学院、班级数据
-- ----------------------------
INSERT INTO `class` (`id`, `class_name`, `college`, `grade`, `major`) VALUES
(1, '软件工程1班', '计算机学院', '2023级', '软件工程'),
(2, '计算机科学与技术2班', '计算机学院', '2023级', '计算机科学与技术'),
(3, '临床医学1班', '医学院', '2023级', '临床医学');

-- ----------------------------
-- 预制区域、楼栋、房间数据
-- area_type: 1区域 2楼栋 3房间
-- ----------------------------
-- 区域
INSERT INTO `area` (`id`, `area_name`, `area_type`, `parent_id`, `level`, `area_code`) VALUES
(1, '南校区', 1, 0, 1, 'SOUTH_CAMPUS'),
(2, '北校区', 1, 0, 1, 'NORTH_CAMPUS');
-- 楼栋
INSERT INTO `area` (`id`, `area_name`, `area_type`, `parent_id`, `level`, `area_code`) VALUES
(101, '南区1栋', 2, 1, 2, 'S1_BUILDING'),
(102, '北区A栋', 2, 2, 2, 'NA_BUILDING');
-- 房间
INSERT INTO `area` (`id`, `area_name`, `area_type`, `parent_id`, `level`, `area_code`) VALUES
(1001, '101宿舍', 3, 101, 3, 'S1_101'),
(1002, '102宿舍', 3, 101, 3, 'S1_102'),
(2001, 'A101宿舍', 3, 102, 3, 'NA_101');

-- ----------------------------
-- 预制用户数据
-- 密码均为 "123456" 的 BCrypt 哈希值: $2a$10$HOc90rCVNwHNHXwEmNORu.R818yss.6KojbvwgL/KRDO596cWpTca
-- ----------------------------
INSERT INTO `user` (`id`, `username`, `student_no`, `password`, `real_name`, `gender`, `phone`, `email`, `class_id`, `room_id`, `status`) VALUES
-- 系统管理员
(1, 'admin', 'admin001', '$2a$10$HOc90rCVNwHNHXwEmNORu.R818yss.6KojbvwgL/KRDO596cWpTca', '超级管理员', 1, '13800000000', 'admin@healthybay.com', NULL, NULL, 1),
-- 班级管理员 (辅导员)
(2, 'teacher_zhang', 'T2023001', '$2a$10$HOc90rCVNwHNHXwEmNORu.R818yss.6KojbvwgL/KRDO596cWpTca', '张老师', 1, '13900000001', 'zhang@healthybay.com', NULL, NULL, 1),
(3, 'teacher_li', 'T2023002', '$2a$10$HOc90rCVNwHNHXwEmNORu.R818yss.6KojbvwgL/KRDO596cWpTca', '李老师', 2, '13900000002', 'li@healthybay.com', NULL, NULL, 1),
-- 学生
(4, 'student1', '20230001', '$2a$10$HOc90rCVNwHNHXwEmNORu.R818yss.6KojbvwgL/KRDO596cWpTca', '张三', 1, '13700000001', 'zhangsan@healthybay.com', 1, 1001, 1),
(5, 'student2', '20230002', '$2a$10$HOc90rCVNwHNHXwEmNORu.R818yss.6KojbvwgL/KRDO596cWpTca', '李四', 1, '13700000002', 'lisi@healthybay.com', 1, 1001, 1),
(6, 'student3', '20230003', '$2a$10$HOc90rCVNwHNHXwEmNORu.R818yss.6KojbvwgL/KRDO596cWpTca', '王五', 2, '13700000003', 'wangwu@healthybay.com', 2, 2001, 1);

-- ----------------------------
-- 用户-角色关联
-- ----------------------------
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 3), -- admin -> system_admin
(2, 2), -- 张老师 -> class_admin
(3, 2), -- 李老师 -> class_admin
(4, 1), -- 张三 -> student
(5, 1), -- 李四 -> student
(6, 1); -- 王五 -> student

-- ----------------------------
-- 班级管理员-班级关联 (manager_class)
-- ----------------------------
INSERT INTO `manager_class` (`manager_id`, `class_id`) VALUES
(2, 1), -- 张老师管理 软件工程1班
(2, 2), -- 张老师管理 计算机科学与技术2班
(3, 3); -- 李老师管理 临床医学1班

-- ----------------------------
-- 预制传染病字典
-- ----------------------------
INSERT INTO `infectious_disease` (`id`, `disease_name`, `disease_code`, `severity_level`, `incubation_period`, `symptoms`, `prevention_measures`) VALUES
(1, '流行性感冒', 'FLU', 1, '1-4天', '发热、咳嗽、喉咙痛、肌肉疼痛', '接种流感疫苗、勤洗手、佩戴口罩'),
(2, '新型冠状病毒肺炎', 'COVID-19', 3, '1-14天', '发热、干咳、乏力、嗅觉味觉减退', '接种新冠疫苗、保持社交距离、佩戴口罩'),
(3, '水痘', 'VARICELLA', 2, '10-21天', '发热、皮疹、瘙痒', '接种水痘疫苗、隔离感染者');
