package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.InfectiousDisease;
import com.fancyframeflare.backend.service.InfectiousDiseaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;

@RestController
@RequestMapping("/api/system/diseases")
@CrossOrigin
@PreAuthorize("hasAuthority('disease:manage')")
public class InfectiousDiseaseController {

    @Autowired
    private InfectiousDiseaseService diseaseService;

    @GetMapping("/page")
    public Result<Page<InfectiousDisease>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String diseaseName) {

        QueryWrapper<InfectiousDisease> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        if (StringUtils.hasText(diseaseName)) {
            queryWrapper.like("disease_name", diseaseName);
        }
        queryWrapper.orderByDesc("create_time");

        Page<InfectiousDisease> page = new Page<>(current, size);
        return Result.success(diseaseService.page(page, queryWrapper));
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<java.util.List<InfectiousDisease>> getList() {
        QueryWrapper<InfectiousDisease> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        queryWrapper.orderByDesc("create_time");
        return Result.success(diseaseService.list(queryWrapper));
    }

    @PostMapping
    @com.fancyframeflare.backend.annotation.LogOperation("新增传染病字典")
    public Result<?> add(@RequestBody InfectiousDisease disease) {
        disease.setCreateTime(new Date());
        disease.setUpdateTime(new Date());
        disease.setDelFlag(0);
        diseaseService.save(disease);
        return Result.success(null);
    }

    @PutMapping
    @com.fancyframeflare.backend.annotation.LogOperation("修改传染病字典")
    public Result<?> update(@RequestBody InfectiousDisease disease) {
        disease.setUpdateTime(new Date());
        diseaseService.updateById(disease);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @com.fancyframeflare.backend.annotation.LogOperation("删除传染病字典")
    public Result<?> delete(@PathVariable Long id) {
        InfectiousDisease disease = new InfectiousDisease();
        disease.setId(id);
        disease.setDelFlag(1); // 逻辑删除
        disease.setUpdateTime(new Date());
        diseaseService.updateById(disease);
        return Result.success(null);
    }
}
