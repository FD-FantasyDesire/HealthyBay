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