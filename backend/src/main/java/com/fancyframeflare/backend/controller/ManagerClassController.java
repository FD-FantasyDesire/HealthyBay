package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.ManagerClass;
import com.fancyframeflare.backend.dto.ManagerClassDTO;
import com.fancyframeflare.backend.dto.ManagerClassQueryDTO;
import com.fancyframeflare.backend.service.ManagerClassService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;

@RestController
@RequestMapping("/api/manager-class")
@CrossOrigin
@PreAuthorize("hasAuthority('class:manage')")
public class ManagerClassController {

    @Autowired
    private ManagerClassService service;

    @GetMapping("/page")
    public Result<Page<ManagerClass>> getPage(ManagerClassQueryDTO queryDTO) {
        Page<ManagerClass> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<ManagerClass> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        // if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
        // // add your keyword search logic here
        // }
        return Result.success(service.page(page, queryWrapper));
    }

    @PostMapping
    public Result<?> add(@RequestBody ManagerClassDTO dto) {
        ManagerClass entity = new ManagerClass();
        BeanUtils.copyProperties(dto, entity);
        entity.setDelFlag(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        service.save(entity);
        return Result.success(null);
    }

    @PutMapping
    public Result<?> update(@RequestBody ManagerClassDTO dto) {
        ManagerClass entity = new ManagerClass();
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }

    @PutMapping("/class/{classId}/manager/{managerId}")
    public Result<?> setClassManager(@PathVariable Long classId, @PathVariable Long managerId) {
        // 先逻辑删除该班级原有的管理员映射
        ManagerClass updateParams = new ManagerClass();
        updateParams.setDelFlag(1);
        updateParams.setUpdateTime(new Date());
        service.update(updateParams, new QueryWrapper<ManagerClass>().eq("class_id", classId));

        // 插入新的管理员
        ManagerClass entity = new ManagerClass();
        entity.setClassId(classId);
        entity.setManagerId(managerId);
        entity.setDelFlag(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        service.save(entity);

        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        ManagerClass entity = new ManagerClass();
        entity.setId(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }
}
