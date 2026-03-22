package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.HealthReport;
import com.fancyframeflare.backend.dto.HealthReportDTO;
import com.fancyframeflare.backend.dto.HealthReportQueryDTO;
import com.fancyframeflare.backend.service.HealthReportService;
import com.fancyframeflare.backend.service.UserService;
import com.fancyframeflare.backend.service.InfectiousDiseaseService;
import com.fancyframeflare.backend.entity.User;
import com.fancyframeflare.backend.entity.InfectiousDisease;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/health-report")
@CrossOrigin
public class HealthReportController {

    @Autowired
    private HealthReportService service;

    @Autowired
    private UserService userService;

    @Autowired
    private InfectiousDiseaseService infectiousDiseaseService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:setting')")
    public Result<Page<HealthReport>> getPage(HealthReportQueryDTO queryDTO) {
        Page<HealthReport> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        QueryWrapper<HealthReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        // if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
        // // add your keyword search logic here
        // }
        queryWrapper.orderByDesc("create_time");
        return Result.success(service.page(page, queryWrapper));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('health:report')")
    public Result<Page<HealthReport>> getMyReports(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null)
            return Result.error("未登录");

        Page<HealthReport> page = new Page<>(current, size);
        QueryWrapper<HealthReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");

        return Result.success(service.page(page, queryWrapper));
    }

    @GetMapping("/class-summary")
    @PreAuthorize("hasAuthority('class:dashboard:view')")
    public Result<List<Map<String, Object>>> getClassSummary(
            @RequestParam Long classId,
            @RequestParam String date) {
        // 1. 获取班级里的所有学生
        List<User> students = userService.list(new QueryWrapper<User>().eq("class_id", classId).eq("del_flag", 0));

        // 2. 获取当天该班级的健康填报记录
        QueryWrapper<HealthReport> reportQuery = new QueryWrapper<>();
        reportQuery.eq("class_id", classId)
                .eq("report_date", date)
                .eq("del_flag", 0);
        List<HealthReport> reports = service.list(reportQuery);

        // 转换成 Map 方便前端展示
        Map<Long, HealthReport> reportMap = reports.stream()
                .collect(Collectors.toMap(HealthReport::getUserId, r -> r, (r1, r2) -> r1));

        List<Map<String, Object>> result = new ArrayList<>();
        for (User student : students) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", student.getId());
            map.put("studentNo", student.getStudentNo());
            map.put("realName", student.getRealName());

            HealthReport report = reportMap.get(student.getId());
            if (report != null) {
                map.put("hasReported", true);
                map.put("temperature", report.getTemperature());
                map.put("symptomJson", report.getSymptomJson());
                map.put("diseaseId", report.getDiseaseId());
                map.put("riskLevel", report.getRiskLevel());
                map.put("reportTime", report.getCreateTime());
            } else {
                map.put("hasReported", false);
            }
            result.add(map);
        }

        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('health:report')")
    public Result<?> add(@RequestBody HealthReportDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("无法获取当前用户信息，请重新登录");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        HealthReport entity = new HealthReport();
        BeanUtils.copyProperties(dto, entity);

        // 强制覆盖从前端传来的关键字段，以当前登录人为准
        entity.setUserId(userId);
        entity.setClassId(user.getClassId());
        entity.setRoomId(user.getRoomId());

        // 如果没有疾病Id，为了兼容无选项可以传0或不传
        if (entity.getDiseaseId() != null && entity.getDiseaseId() == 0) {
            entity.setDiseaseId(null);
        }

        // 因子评分算法计算 risk_level: 1低 2中 3高
        int score = 0;

        // 1. 体温因子
        if (entity.getTemperature() != null) {
            double temp = entity.getTemperature().doubleValue();
            if (temp >= 39.0)
                score += 50;
            else if (temp >= 37.3)
                score += 30;
        }

        // 2. 症状因子 (symptomJson)
        if (entity.getSymptomJson() != null && !entity.getSymptomJson().isEmpty()
                && !entity.getSymptomJson().contains("无症状")) {
            // 每有一个症状加 10 分
            int symptomCount = entity.getSymptomJson().split(",").length;
            score += symptomCount * 10;
        }

        // 3. 接触史因子
        if (entity.getExposureHistory() != null && !entity.getExposureHistory().trim().isEmpty()
                && !entity.getExposureHistory().equals("无")) {
            score += 40;
        }

        // 4. 疾病因子
        if (entity.getDiseaseId() != null) {
            InfectiousDisease disease = infectiousDiseaseService.getById(entity.getDiseaseId());
            if (disease != null && disease.getSeverityLevel() != null) {
                // severityLevel: 1轻微, 2中度, 3严重
                score += disease.getSeverityLevel() * 30;
            } else {
                score += 50; // 默认加50
            }
        }

        // 5. 疫苗接种因子 (vaccinationStatus: 1已接种 2未接种 3部分接种)
        if (entity.getVaccinationStatus() != null) {
            if (entity.getVaccinationStatus() == 1)
                score -= 20;
            else if (entity.getVaccinationStatus() == 3)
                score -= 10;
        }

        // 综合评估风险等级
        if (score >= 60) {
            entity.setRiskLevel(3); // 高风险
        } else if (score >= 30) {
            entity.setRiskLevel(2); // 中风险
        } else {
            entity.setRiskLevel(1); // 低风险
        }

        entity.setDelFlag(0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setReportDate(new Date());
        if (entity.getStatus() == null) {
            entity.setStatus(0); // 未处理
        }

        // Save the report
        service.save(entity);

        // Trigger Warning Engine
        service.triggerWarningEngine(entity);

        return Result.success(null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:setting')")
    public Result<?> update(@RequestBody HealthReportDTO dto) {
        HealthReport entity = new HealthReport();
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:setting')")
    public Result<?> delete(@PathVariable Long id) {
        HealthReport entity = new HealthReport();
        entity.setId(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(new Date());
        service.updateById(entity);
        return Result.success(null);
    }
}
