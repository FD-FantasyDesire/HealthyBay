package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.OperationLog;
import com.fancyframeflare.backend.dto.OperationLogDTO;
import com.fancyframeflare.backend.dto.OperationLogQueryDTO;
import com.fancyframeflare.backend.service.OperationLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;

@RestController
@RequestMapping("/api/operation-log")
@CrossOrigin
@PreAuthorize("hasAuthority('log:manage')")
public class OperationLogController {

    @Autowired
    private OperationLogService service;

    @GetMapping("/page")
    public Result<Page<OperationLog>> getPage(OperationLogQueryDTO queryDTO) {
        Page<OperationLog> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        // if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
        // // add your keyword search logic here
        // }
        return Result.success(service.page(page, queryWrapper));
    }

    @PostMapping
    public Result<?> add(@RequestBody OperationLogDTO dto) {
        OperationLog entity = new OperationLog();
        BeanUtils.copyProperties(dto, entity);
        entity.setDelFlag(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        service.save(entity);
        return Result.success(null);
    }

    @PutMapping
    public Result<?> update(@RequestBody OperationLogDTO dto) {
        OperationLog entity = new OperationLog();
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        OperationLog entity = new OperationLog();
        entity.setId(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }
}
