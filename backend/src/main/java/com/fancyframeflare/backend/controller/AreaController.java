package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.Area;
import com.fancyframeflare.backend.dto.AreaDTO;
import com.fancyframeflare.backend.dto.AreaQueryDTO;
import com.fancyframeflare.backend.service.AreaService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/area")
@CrossOrigin
@PreAuthorize("hasAuthority('system:setting')")
public class AreaController {

    @Autowired
    private AreaService service;

    @GetMapping("/page")
    public Result<Page<Area>> getPage(AreaQueryDTO queryDTO) {
        Page<Area> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<Area> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like("area_name", queryDTO.getKeyword())
                    .or()
                    .like("area_code", queryDTO.getKeyword());
        }
        if (queryDTO.getAreaType() != null) {
            queryWrapper.eq("area_type", queryDTO.getAreaType());
        }
        if (queryDTO.getParentId() != null) {
            queryWrapper.eq("parent_id", queryDTO.getParentId());
        }

        queryWrapper.orderByAsc("level", "id");
        return Result.success(service.page(page, queryWrapper));
    }

    @GetMapping("/tree")
    public Result<List<AreaDTO>> getTree() {
        QueryWrapper<Area> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        queryWrapper.orderByAsc("level", "id");
        List<Area> list = service.list(queryWrapper);
        List<AreaDTO> tree = buildTree(list, 0L);
        return Result.success(tree);
    }

    private List<AreaDTO> buildTree(List<Area> list, Long parentId) {
        List<AreaDTO> tree = new ArrayList<>();
        for (Area area : list) {
            if (area.getParentId() != null && area.getParentId().equals(parentId)) {
                AreaDTO dto = new AreaDTO();
                BeanUtils.copyProperties(area, dto);
                dto.setChildren(buildTree(list, area.getId()));
                tree.add(dto);
            }
        }
        return tree;
    }

    @PostMapping
    public Result<?> add(@RequestBody AreaDTO dto) {
        Area entity = new Area();
        BeanUtils.copyProperties(dto, entity);
        entity.setDelFlag(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        service.save(entity);
        return Result.success(null);
    }

    @PutMapping
    public Result<?> update(@RequestBody AreaDTO dto) {
        Area entity = new Area();
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }

    @PutMapping("/{id}/manager/{managerId}")
    public Result<?> setManager(@PathVariable Long id, @PathVariable Long managerId) {
        Area entity = new Area();
        entity.setId(id);
        entity.setManagerId(managerId);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // Check if there are children
        QueryWrapper<Area> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        queryWrapper.eq("del_flag", 0);
        long count = service.count(queryWrapper);
        if (count > 0) {
            return Result.error("存在子节点，无法删除");
        }

        Area entity = new Area();
        entity.setId(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }
}
