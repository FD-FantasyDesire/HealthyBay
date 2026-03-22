package com.fancyframeflare.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.entity.HealthReport;
import com.fancyframeflare.backend.entity.Notice;
import com.fancyframeflare.backend.entity.User;
import com.fancyframeflare.backend.entity.WarningRecord;
import com.fancyframeflare.backend.service.HealthReportService;
import com.fancyframeflare.backend.service.NoticeService;
import com.fancyframeflare.backend.service.UserService;
import com.fancyframeflare.backend.service.WarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
@PreAuthorize("hasAuthority('dashboard:view')")
public class DashboardController {

        @Autowired
        private UserService userService;

        @Autowired
        private HealthReportService healthReportService;

        @Autowired
        private WarningRecordService warningRecordService;

        @Autowired
        private NoticeService noticeService;

        @GetMapping("/summary")
        public Result<Map<String, Object>> getSummary() {
                Map<String, Object> summary = new HashMap<>();

                // 1. Total users (students and staff)
                long totalUsers = userService.count(new QueryWrapper<User>().eq("del_flag", 0));
                summary.put("totalUsers", totalUsers);

                // 2. Today's health reports
                String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                long todayReports = healthReportService.count(
                                new QueryWrapper<HealthReport>()
                                                .eq("del_flag", 0)
                                                .eq("report_date", today));
                summary.put("todayReports", todayReports);

                // 3. Active warnings (status = 0)
                long activeWarnings = warningRecordService.count(
                                new QueryWrapper<WarningRecord>()
                                                .eq("del_flag", 0)
                                                .eq("status", 0));
                summary.put("activeWarnings", activeWarnings);

                // 4. Total notices published
                long totalNotices = noticeService.count(
                                new QueryWrapper<Notice>()
                                                .eq("del_flag", 0)
                                                .eq("status", 1));
                summary.put("totalNotices", totalNotices);

                return Result.success(summary);
        }

        @GetMapping("/trend")
        public Result<Map<String, Object>> getTrend() {
                List<String> dates = new ArrayList<>();
                List<Long> totalReports = new ArrayList<>();
                List<Long> abnormalReports = new ArrayList<>();

                LocalDate today = LocalDate.now();

                // Get data for the last 7 days
                for (int i = 6; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                        dates.add(dateStr);

                        // Total reports for the day
                        long total = healthReportService.count(
                                        new QueryWrapper<HealthReport>()
                                                        .eq("del_flag", 0)
                                                        .eq("report_date", dateStr));
                        totalReports.add(total);

                        // Abnormal reports for the day (riskLevel > 1)
                        long abnormal = healthReportService.count(
                                        new QueryWrapper<HealthReport>()
                                                        .eq("del_flag", 0)
                                                        .eq("report_date", dateStr)
                                                        .gt("risk_level", 1));
                        abnormalReports.add(abnormal);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("dates", dates);
                result.put("totalReports", totalReports);
                result.put("abnormalReports", abnormalReports);

                return Result.success(result);
        }
}
