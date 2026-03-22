package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.WarningRule;
import com.fancyframeflare.backend.dto.WarningRuleDTO;
import com.fancyframeflare.backend.dto.WarningRuleQueryDTO;
import com.fancyframeflare.backend.service.WarningRuleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;

@RestController
@RequestMapping("/api/warning-rule")
@CrossOrigin
@PreAuthorize("hasAuthority('rule:manage')")
public class WarningRuleController {

    @Autowired
    private WarningRuleService service;

    @GetMapping("/page")
    public Result<Page<WarningRule>> getPage(WarningRuleQueryDTO queryDTO) {
        Page<WarningRule> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<WarningRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like("rule_name", queryDTO.getKeyword());
        }
        if (queryDTO.getEnabled() != null) {
            queryWrapper.eq("enabled", queryDTO.getEnabled());
        }

        queryWrapper.orderByDesc("id");
        return Result.success(service.page(page, queryWrapper));
    }

    @PostMapping
    public Result<?> add(@RequestBody WarningRuleDTO dto) {
        WarningRule entity = new WarningRule();
        BeanUtils.copyProperties(dto, entity);
        entity.setDelFlag(0);
        if (entity.getEnabled() == null) {
            entity.setEnabled(1);
        }
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        service.save(entity);
        return Result.success(null);
    }

    @PutMapping
    public Result<?> update(@RequestBody WarningRuleDTO dto) {
        WarningRule entity = new WarningRule();
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        WarningRule entity = new WarningRule();
        entity.setId(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }
}
