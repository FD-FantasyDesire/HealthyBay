package com.fancyframeflare.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancyframeflare.backend.dto.LoginDTO;
import com.fancyframeflare.backend.dto.RegisterDTO;
import com.fancyframeflare.backend.entity.Permission;
import com.fancyframeflare.backend.entity.Role;
import com.fancyframeflare.backend.entity.RolePermission;
import com.fancyframeflare.backend.entity.User;
import com.fancyframeflare.backend.entity.UserRole;
import com.fancyframeflare.backend.mapper.PermissionMapper;
import com.fancyframeflare.backend.mapper.RoleMapper;
import com.fancyframeflare.backend.mapper.RolePermissionMapper;
import com.fancyframeflare.backend.mapper.UserMapper;
import com.fancyframeflare.backend.mapper.UserRoleMapper;
import com.fancyframeflare.backend.service.UserService;
import com.fancyframeflare.backend.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Value("${app.security.enable-permissions:true}")
    private boolean enablePermissions;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 校验学号/用户名唯一性
        Long count = baseMapper.selectCount(
                new QueryWrapper<User>().eq("student_no", dto.getStudentNo()).or().eq("username", dto.getUsername()));
        if (count > 0) {
            throw new RuntimeException("学号或用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setStudentNo(dto.getStudentNo());
        // 加密存储密码
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        user.setDelFlag(0);

        baseMapper.insert(user);

        // 默认分配"学生"角色。假设学生角色ID为1 (可在配置表或常量中定义)
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(1L); // 1 代表学生
        userRole.setDelFlag(0);
        userRoleMapper.insert(userRole);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = baseMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 获取用户角色与权限
        List<UserRole> userRoles = userRoleMapper.selectList(new QueryWrapper<UserRole>().eq("user_id", user.getId()));
        List<String> roles = new ArrayList<>();
        Set<String> permissions = new HashSet<>();

        if (!enablePermissions) {
            roles.add("system_admin"); // 使用 system_admin 或 admin 都可以被前端识别为超管
            permissions.add("*:*:*");
        } else if (userRoles != null && !userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());

            // 查 role 组装角色标识列表
            List<Role> roleList = roleMapper.selectBatchIds(roleIds);
            if (roleList != null && !roleList.isEmpty()) {
                roles = roleList.stream().map(Role::getRoleKey).collect(Collectors.toList());
            }

            // 查 role_permission
            List<RolePermission> rolePermissions = rolePermissionMapper
                    .selectList(new QueryWrapper<RolePermission>().in("role_id", roleIds));
            if (rolePermissions != null && !rolePermissions.isEmpty()) {
                List<Long> permIds = rolePermissions.stream().map(RolePermission::getPermissionId).distinct()
                        .collect(Collectors.toList());

                // 查 permission 组装权限标识列表
                List<Permission> permList = permissionMapper.selectBatchIds(permIds);
                if (permList != null && !permList.isEmpty()) {
                    permissions = permList.stream().map(Permission::getPermKey).collect(Collectors.toSet());
                }
            }
        }

        // 可以选择将角色和权限一起合并，也可以分别存放，通常前端会一起当作 auths 处理，或者用 JwtUtils 重载方法放入 token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("classId", user.getClassId());
        claims.put("roomId", user.getRoomId());
        // 将角色和权限塞进 JWT claims (注意不要塞太多导致 token 过长)
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        // 生成Token (使用更灵活的重载生成方法，或者依然使用旧方法但把信息放Redis。这里我们将角色权限一并加入JWT)
        String token = jwtUtils.generateToken(user.getUsername(), claims);

        // Redis存储Token
        String redisKey = "user:token:" + user.getId();
        redisTemplate.opsForValue().set(redisKey, token, 24, TimeUnit.HOURS);

        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("userId", user.getId());
        res.put("username", user.getUsername());
        res.put("roles", roles);
        res.put("permissions", permissions);
        return res;
    }
}
