package com.fancyframeflare.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fancyframeflare.backend.entity.HealthReport;

public interface HealthReportService extends IService<HealthReport> {
    void triggerWarningEngine(HealthReport newReport);
}
