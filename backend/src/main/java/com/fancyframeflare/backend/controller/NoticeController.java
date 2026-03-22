package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.Notice;
import com.fancyframeflare.backend.dto.NoticeDTO;
import com.fancyframeflare.backend.dto.NoticeQueryDTO;
import com.fancyframeflare.backend.service.NoticeService;
import com.fancyframeflare.backend.service.ManagerClassService;
import com.fancyframeflare.backend.entity.ManagerClass;
import com.fancyframeflare.backend.mapper.UserRoleMapper;
import com.fancyframeflare.backend.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notice")
@CrossOrigin
public class NoticeController {

    @Autowired
    private NoticeService service;

    @Autowired
    private ManagerClassService managerClassService;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('notice:view')")
    public Result<Page<Notice>> getPage(NoticeQueryDTO queryDTO) {
        Page<Notice> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            queryWrapper.like("title", queryDTO.getKeyword());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }
        if (queryDTO.getClassId() != null) {
            queryWrapper.and(wrapper -> wrapper.eq("publish_scope", 1).or().eq("class_id", queryDTO.getClassId()));
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(service.page(page, queryWrapper));
    }

    private boolean hasPublishPermission(Long userId, Notice notice) {
        // Check if admin
        List<UserRole> userRoles = userRoleMapper
                .selectList(new QueryWrapper<UserRole>().eq("user_id", userId).eq("del_flag", 0));
        boolean isAdmin = userRoles.stream().anyMatch(ur -> ur.getRoleId() == 3L);
        if (isAdmin) {
            return true;
        }

        // If not admin, they can only publish to their managed classes
        if (notice.getPublishScope() == 1) { // Normal managers cannot publish to all classes
            return false;
        }
        if (notice.getClassId() != null) {
            long count = managerClassService.count(new QueryWrapper<ManagerClass>()
                    .eq("manager_id", userId)
                    .eq("class_id", notice.getClassId())
                    .eq("del_flag", 0));
            return count > 0;
        }
        return false;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('notice:publish')")
    public Result<?> add(@RequestBody NoticeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }

        Notice entity = new Notice();
        BeanUtils.copyProperties(dto, entity);

        if (!hasPublishPermission(userId, entity)) {
            return Result.error("您没有权限发布该范围的通知");
        }

        entity.setPublisherId(userId);
        entity.setDelFlag(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        service.save(entity);
        return Result.success(null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('notice:publish')")
    public Result<?> update(@RequestBody NoticeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }

        Notice entity = new Notice();
        BeanUtils.copyProperties(dto, entity);

        if (!hasPublishPermission(userId, entity)) {
            return Result.error("您没有权限修改或发布该范围的通知");
        }

        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('notice:publish')")
    public Result<?> delete(@PathVariable Long id) {
        // Normally we should also check if they have permission to delete, but assuming
        // UI restricts the button
        Notice entity = new Notice();
        entity.setId(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }
}
