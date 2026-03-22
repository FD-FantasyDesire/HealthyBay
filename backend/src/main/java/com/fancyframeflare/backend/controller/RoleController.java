package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.Permission;
import com.fancyframeflare.backend.entity.Role;
import com.fancyframeflare.backend.entity.RolePermission;
import com.fancyframeflare.backend.mapper.PermissionMapper;
import com.fancyframeflare.backend.mapper.RoleMapper;
import com.fancyframeflare.backend.mapper.RolePermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/roles")
@CrossOrigin
@PreAuthorize("hasAuthority('role:manage')")
public class RoleController {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    // 1. 获取角色列表
    @GetMapping("/page")
    public Result<Page<Role>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Role> page = new Page<>(current, size);
        return Result.success(roleMapper.selectPage(page, new QueryWrapper<Role>().eq("del_flag", 0)));
    }

    // 2. 获取所有权限字典（供角色分配权限时选择）
    @GetMapping("/permissions")
    public Result<List<Permission>> getAllPermissions() {
        return Result.success(permissionMapper.selectList(new QueryWrapper<Permission>().eq("del_flag", 0)));
    }

    // 3. 获取某个角色已拥有的权限ID列表
    @GetMapping("/{roleId}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        List<RolePermission> rps = rolePermissionMapper.selectList(
                new QueryWrapper<RolePermission>().eq("role_id", roleId).eq("del_flag", 0));
        List<Long> permIds = rps.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
        return Result.success(permIds);
    }

    // 4. 给角色分配权限
    @PostMapping("/{roleId}/permissions")
    @Transactional
    public Result<?> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        // 先删除原有的关联
        rolePermissionMapper.delete(new QueryWrapper<RolePermission>().eq("role_id", roleId));

        // 重新插入新的关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permId);
                rp.setDelFlag(0);
                rp.setCreateTime(new Date());
                rp.setUpdateTime(new Date());
                rolePermissionMapper.insert(rp);
            }
        }
        return Result.success(null);
    }

    // 5. 新增角色
    @PostMapping
    public Result<?> addRole(@RequestBody Role role) {
        role.setDelFlag(0);
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());
        roleMapper.insert(role);
        return Result.success(null);
    }

    // 6. 修改角色
    @PutMapping
    public Result<?> updateRole(@RequestBody Role role) {
        role.setUpdateTime(new Date());
        roleMapper.updateById(role);
        return Result.success(null);
    }

    // 7. 删除角色
    @DeleteMapping("/{id}")
    @Transactional
    public Result<?> deleteRole(@PathVariable Long id) {
        // 逻辑删除
        Role role = new Role();
        role.setId(id);
        role.setDelFlag(1);
        roleMapper.updateById(role);

        // 删除其权限关联
        rolePermissionMapper.delete(new QueryWrapper<RolePermission>().eq("role_id", id));
        return Result.success(null);
    }
}
