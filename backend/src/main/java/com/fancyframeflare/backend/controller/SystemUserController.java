package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.User;
import com.fancyframeflare.backend.entity.UserRole;
import com.fancyframeflare.backend.mapper.UserRoleMapper;
import com.fancyframeflare.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/users")
@CrossOrigin
public class SystemUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<Page<User>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("username", keyword)
                    .or()
                    .like("real_name", keyword)
                    .or()
                    .like("student_no", keyword));
        }
        queryWrapper.orderByDesc("create_time");

        Page<User> page = new Page<>(current, size);
        Page<User> result = userService.page(page, queryWrapper);

        // 擦除密码避免泄露
        for (User u : result.getRecords()) {
            u.setPassword(null);
        }

        return Result.success(result);
    }

    @GetMapping("/assignable")
    @PreAuthorize("hasAnyAuthority('user:manage', 'class:manage', 'system:setting')")
    public Result<Page<User>> getAssignableUsers(
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Boolean unassignedClass,
            @RequestParam(required = false) Boolean unassignedRoom,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);

        if (roleId != null) {
            List<UserRole> userRoles = userRoleMapper
                    .selectList(new QueryWrapper<UserRole>().eq("role_id", roleId).eq("del_flag", 0));
            if (userRoles.isEmpty()) {
                return Result.success(new Page<>(current, size));
            }
            List<Long> userIds = userRoles.stream().map(UserRole::getUserId).collect(Collectors.toList());
            queryWrapper.in("id", userIds);
        }

        if (Boolean.TRUE.equals(unassignedClass)) {
            queryWrapper.isNull("class_id");
        }
        if (Boolean.TRUE.equals(unassignedRoom)) {
            queryWrapper.isNull("room_id");
        }

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("username", keyword)
                    .or()
                    .like("real_name", keyword)
                    .or()
                    .like("student_no", keyword));
        }

        Page<User> page = new Page<>(current, size);
        Page<User> result = userService.page(page, queryWrapper);
        for (User u : result.getRecords()) {
            u.setPassword(null);
        }
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<?> add(@RequestBody User user) {
        // 校验用户名是否存在
        long count = userService.count(new QueryWrapper<User>().eq("username", user.getUsername()).eq("del_flag", 0));
        if (count > 0) {
            return Result.error("用户名已存在");
        }

        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword("123456"); // 默认密码
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setDelFlag(0);
        if (user.getStatus() == null) {
            user.setStatus(1); // 正常
        }

        userService.save(user);

        // 为后台新增的用户默认分配“学生”权限(ID=1)
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(1L);
        userRole.setDelFlag(0);
        userRole.setCreateTime(new Date());
        userRole.setUpdateTime(new Date());
        userRoleMapper.insert(userRole);

        return Result.success(null);
    }

    @PutMapping("/assign-class")
    @PreAuthorize("hasAuthority('class:manage')")
    public Result<?> assignClass(@RequestParam Long classId, @RequestBody List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.error("用户列表不能为空");
        }
        User updateParams = new User();
        updateParams.setClassId(classId);
        updateParams.setUpdateTime(new Date());
        userService.update(updateParams, new QueryWrapper<User>().in("id", userIds));
        return Result.success(null);
    }

    @PutMapping("/assign-room")
    @PreAuthorize("hasAuthority('system:setting')")
    public Result<?> assignRoom(@RequestParam Long roomId, @RequestBody List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.error("用户列表不能为空");
        }
        User updateParams = new User();
        updateParams.setRoomId(roomId);
        updateParams.setUpdateTime(new Date());
        userService.update(updateParams, new QueryWrapper<User>().in("id", userIds));
        return Result.success(null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<?> update(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }

        // 如果密码不为空，则更新密码，否则保持原样（不更新密码字段）
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // null表示不更新该字段
        }

        user.setUpdateTime(new Date());
        userService.updateById(user);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<?> delete(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        user.setDelFlag(1); // 逻辑删除
        user.setUpdateTime(new Date());
        userService.updateById(user);
        return Result.success(null);
    }

    // 更新用户状态（启用/禁用）
    @PutMapping("/{id}/status/{status}")
    @com.fancyframeflare.backend.annotation.LogOperation("修改用户状态")
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<?> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(new Date());
        userService.updateById(user);
        return Result.success(null);
    }
}
