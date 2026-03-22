package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.WarningRecord;
import com.fancyframeflare.backend.entity.WarningRecordDetail;
import com.fancyframeflare.backend.entity.User;
import com.fancyframeflare.backend.dto.WarningRecordDTO;
import com.fancyframeflare.backend.dto.WarningRecordQueryDTO;
import com.fancyframeflare.backend.service.WarningRecordService;
import com.fancyframeflare.backend.service.WarningRecordDetailService;
import com.fancyframeflare.backend.service.UserService;
import com.fancyframeflare.backend.entity.HealthReport;
import com.fancyframeflare.backend.service.HealthReportService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/warning-record")
@CrossOrigin
@PreAuthorize("hasAuthority('warning:view')")
public class WarningRecordController {

    @Autowired
    private WarningRecordService service;

    @Autowired
    private WarningRecordDetailService warningRecordDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private HealthReportService healthReportService;

    @PostMapping("/page")
    public Result<Page<WarningRecord>> getPage(@RequestBody WarningRecordQueryDTO queryDTO) {
        Page<WarningRecord> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<WarningRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);

        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }

        if (queryDTO.getClassId() != null) {
            queryWrapper.eq("class_id", queryDTO.getClassId());
        }

        // Scope for class managers
        if (queryDTO.getManagerClassIds() != null && !queryDTO.getManagerClassIds().isEmpty()) {
            queryWrapper.in("class_id", queryDTO.getManagerClassIds());
        }

        queryWrapper.orderByDesc("status", "id"); // 0 (unprocessed) first
        return Result.success(service.page(page, queryWrapper));
    }

    @GetMapping("/{id}/details")
    public Result<List<Map<String, Object>>> getDetails(@PathVariable Long id) {
        List<WarningRecordDetail> details = warningRecordDetailService.list(
                new QueryWrapper<WarningRecordDetail>().eq("warning_record_id", id).eq("del_flag", 0));

        List<Map<String, Object>> result = new ArrayList<>();
        for (WarningRecordDetail detail : details) {
            Map<String, Object> map = new HashMap<>();
            map.put("detailId", detail.getId());

            if (detail.getUserId() != null) {
                User user = userService.getById(detail.getUserId());
                if (user != null) {
                    map.put("studentNo", user.getStudentNo());
                    map.put("realName", user.getRealName());
                }
            }

            if (detail.getHealthReportId() != null) {
                HealthReport report = healthReportService.getById(detail.getHealthReportId());
                if (report != null) {
                    map.put("temperature", report.getTemperature());
                    map.put("symptoms", report.getSymptomJson());
                    map.put("reportTime", report.getCreateTime());
                    map.put("riskLevel", report.getRiskLevel());
                }
            }
            result.add(map);
        }
        return Result.success(result);
    }

    @PostMapping
    public Result<?> add(@RequestBody WarningRecordDTO dto) {
        WarningRecord entity = new WarningRecord();
        BeanUtils.copyProperties(dto, entity);
        entity.setDelFlag(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        service.save(entity);
        return Result.success(null);
    }

    @PutMapping
    public Result<?> update(@RequestBody WarningRecordDTO dto) {
        WarningRecord entity = new WarningRecord();
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        WarningRecord entity = new WarningRecord();
        entity.setId(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }
}
