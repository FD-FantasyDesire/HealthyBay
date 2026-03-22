package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.ClassInfo;
import com.fancyframeflare.backend.dto.ClassInfoDTO;
import com.fancyframeflare.backend.dto.ClassInfoQueryDTO;
import com.fancyframeflare.backend.service.ClassInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import com.fancyframeflare.backend.entity.UserRole;
import com.fancyframeflare.backend.mapper.UserRoleMapper;
import com.fancyframeflare.backend.service.ManagerClassService;
import com.fancyframeflare.backend.entity.ManagerClass;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/class")
@CrossOrigin
public class ClassInfoController {

    @Autowired
    private ClassInfoService service;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private ManagerClassService managerClassService;

    @GetMapping("/managed")
    @PreAuthorize("hasAnyAuthority('class:manage', 'class:dashboard:view', 'notice:publish')")
    public Result<List<ClassInfo>> getManagedClasses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null)
            return Result.error("未登录");

        // 检查是否为超级管理员或系统管理员 (role_id = 3)
        List<UserRole> userRoles = userRoleMapper
                .selectList(new QueryWrapper<UserRole>().eq("user_id", userId).eq("del_flag", 0));
        boolean isAdmin = userRoles.stream().anyMatch(ur -> ur.getRoleId() == 3L);

        if (isAdmin) {
            return Result.success(service.list(new QueryWrapper<ClassInfo>().eq("del_flag", 0)));
        } else {
            List<ManagerClass> mcs = managerClassService
                    .list(new QueryWrapper<ManagerClass>().eq("manager_id", userId).eq("del_flag", 0));
            if (mcs.isEmpty()) {
                return Result.success(Collections.emptyList());
            }
            List<Long> classIds = mcs.stream().map(ManagerClass::getClassId).collect(Collectors.toList());
            return Result.success(service.list(new QueryWrapper<ClassInfo>().in("id", classIds).eq("del_flag", 0)));
        }
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('class:manage')")
    public Result<Page<ClassInfo>> getPage(ClassInfoQueryDTO queryDTO) {
        Page<ClassInfo> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<ClassInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper.like("class_name", queryDTO.getKeyword())
                    .or()
                    .like("college", queryDTO.getKeyword())
                    .or()
                    .like("major", queryDTO.getKeyword()));
        }

        queryWrapper.orderByDesc("id");
        return Result.success(service.page(page, queryWrapper));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('class:manage')")
    public Result<?> add(@RequestBody ClassInfoDTO dto) {
        ClassInfo entity = new ClassInfo();
        BeanUtils.copyProperties(dto, entity);
        entity.setDelFlag(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        service.save(entity);
        return Result.success(null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('class:manage')")
    public Result<?> update(@RequestBody ClassInfoDTO dto) {
        ClassInfo entity = new ClassInfo();
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('class:manage')")
    public Result<?> delete(@PathVariable Long id) {
        ClassInfo entity = new ClassInfo();
        entity.setId(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }
}
