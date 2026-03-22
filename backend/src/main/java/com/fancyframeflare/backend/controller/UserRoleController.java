package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.Role;
import com.fancyframeflare.backend.entity.UserRole;
import com.fancyframeflare.backend.mapper.RoleMapper;
import com.fancyframeflare.backend.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/user-roles")
@CrossOrigin
@PreAuthorize("hasAuthority('role:manage')")
public class UserRoleController {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    // 1. 获取所有可用角色列表（供用户分配角色时选择）
    @GetMapping("/roles")
    public Result<List<Role>> getAllRoles() {
        return Result.success(roleMapper.selectList(new QueryWrapper<Role>().eq("del_flag", 0)));
    }

    // 2. 获取某个用户已拥有的角色ID列表
    @GetMapping("/{userId}")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new QueryWrapper<UserRole>().eq("user_id", userId).eq("del_flag", 0));
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        return Result.success(roleIds);
    }

    // 3. 给用户分配角色
    @PostMapping("/{userId}")
    @Transactional
    public Result<?> assignRolesToUser(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        // 先删除用户原有的角色关联
        userRoleMapper.delete(new QueryWrapper<UserRole>().eq("user_id", userId));

        // 插入新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                ur.setDelFlag(0);
                ur.setCreateTime(new Date());
                ur.setUpdateTime(new Date());
                userRoleMapper.insert(ur);
            }
        }
        return Result.success(null);
    }
}
