# 基于数据库结构的权限划分登录系统

本项目实现了基于数据库结构的权限划分登录系统，使用JWT、Redis、BCrypt加密和MyBatis-Plus技术栈。

## 技术架构

- Spring Boot 3.5.11
- Java 17
- MySQL数据库
- Redis缓存
- JWT令牌认证
- BCrypt密码加密
- MyBatis-Plus ORM框架
- Spring Security权限控制

## 数据库表结构

系统基于以下表结构实现权限管理：

- `user`：用户表
- `role`：角色表
- `permission`：权限表
- `user_role`：用户-角色关联表
- `role_permission`：角色-权限关联表

## 功能特性

### 1. 用户认证
- 用户登录：通过用户名和密码进行身份验证
- JWT令牌生成和验证
- 自动密码加密（BCrypt）

### 2. 权限管理
- 基于角色的权限控制（RBAC）
- 细粒度的权限验证
- 支持多角色分配

### 3. 安全特性
- 密码BCrypt加密存储
- JWT令牌自动过期
- Redis存储活跃会话
- 防止常见安全漏洞

## API接口

### 认证相关

#### 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}

响应：
{
  "success": true,
  "message": "Login successful",
  "token": "jwt_token_here",
  "user": { ... }
}
```

#### 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "new_username",
  "password": "new_password",
  "realName": "Real Name",
  "email": "email@example.com"
}
```

#### 用户登出
```
POST /api/auth/logout
Authorization: Bearer {jwt_token}
```

#### 检查权限
```
GET /api/auth/check-permission/{permission}
Authorization: Bearer {jwt_token}
```

### 用户管理相关

#### 获取用户资料
```
GET /api/auth/profile
Authorization: Bearer {jwt_token}
```

#### 获取用户列表（需权限）
```
GET /api/user/list
Authorization: Bearer {jwt_token}
```

### 测试接口

#### 管理员权限测试
```
GET /api/test/admin
Authorization: Bearer {jwt_token}
```

#### 普通用户权限测试
```
GET /api/test/user
Authorization: Bearer {jwt_token}
```

## 权限说明

系统支持以下预定义权限：
- `admin`: 管理员权限
- `user`: 普通用户权限
- `student`: 学生权限
- `teacher`: 教师权限

可以根据业务需求扩展更多权限。

## 配置说明

### application.properties
```properties
# 数据库配置
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/healthy_bay?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=

# Redis配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.database=0
spring.redis.timeout=2000ms

# JWT配置
jwt.secret=mySecretKey
jwt.expiration=86400  # 24小时
```

## 部署说明

1. 确保MySQL和Redis服务已启动
2. 创建数据库并执行初始化脚本
3. 修改application.properties中的数据库连接信息
4. 启动Spring Boot应用

## 安全最佳实践

1. 生产环境中务必使用强密码
2. JWT密钥应从环境变量或配置中心获取
3. 定期更换密钥
4. 实施适当的速率限制
5. 对敏感操作添加二次验证

## 扩展功能

- 可以轻松扩展更多的权限级别
- 支持动态权限分配
- 可集成第三方认证（OAuth2、LDAP等）
- 支持单点登录（SSO）