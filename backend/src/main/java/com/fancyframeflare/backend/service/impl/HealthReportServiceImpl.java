package com.fancyframeflare.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancyframeflare.backend.entity.HealthReport;
import com.fancyframeflare.backend.entity.WarningRule;
import com.fancyframeflare.backend.entity.WarningRecord;
import com.fancyframeflare.backend.entity.WarningRecordDetail;
import com.fancyframeflare.backend.mapper.HealthReportMapper;
import com.fancyframeflare.backend.service.HealthReportService;
import com.fancyframeflare.backend.service.WarningRuleService;
import com.fancyframeflare.backend.service.WarningRecordService;
import com.fancyframeflare.backend.service.WarningRecordDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HealthReportServiceImpl extends ServiceImpl<HealthReportMapper, HealthReport>
        implements HealthReportService {

    @Autowired
    private WarningRuleService warningRuleService;

    @Autowired
    private WarningRecordService warningRecordService;

    @Autowired
    private WarningRecordDetailService warningRecordDetailService;

    @Async
    @Override
    public void triggerWarningEngine(HealthReport newReport) {
        if (newReport == null || newReport.getClassId() == null) {
            return;
        }

        // Get all enabled rules
        QueryWrapper<WarningRule> ruleWrapper = new QueryWrapper<>();
        ruleWrapper.eq("del_flag", 0).eq("enabled", 1);
        List<WarningRule> rules = warningRuleService.list(ruleWrapper);

        for (WarningRule rule : rules) {
            // Check if rule applies to this class
            if (rule.getClassScope() != null && !rule.getClassScope().equals(newReport.getClassId())) {
                continue;
            }

            // Check if rule applies to this disease
            if (rule.getDiseaseId() != null) {
                if (newReport.getDiseaseId() == null || !rule.getDiseaseId().equals(newReport.getDiseaseId())) {
                    continue;
                }
            } else {
                // If diseaseId in rule is null, it means any abnormal report.
                // We should check if the newReport is abnormal (e.g., risk_level > 1 or has
                // diseaseId)
                if (newReport.getRiskLevel() != null && newReport.getRiskLevel() <= 1
                        && newReport.getDiseaseId() == null) {
                    continue;
                }
            }

            // Calculate time window
            long timeWindowMillis = rule.getTimeWindowHours() * 60 * 60 * 1000L;
            Date startTime = new Date(System.currentTimeMillis() - timeWindowMillis);

            // Count distinct users in this class with this disease (or abnormal if rule
            // disease is null) in time window
            QueryWrapper<HealthReport> countWrapper = new QueryWrapper<>();
            countWrapper.eq("del_flag", 0)
                    .eq("class_id", newReport.getClassId())
                    .ge("create_time", startTime);

            if (rule.getDiseaseId() != null) {
                countWrapper.eq("disease_id", rule.getDiseaseId());
            } else {
                countWrapper.and(w -> w.gt("risk_level", 1).or().isNotNull("disease_id"));
            }

            // Using select count(distinct user_id)
            countWrapper.select("count(distinct user_id) as count");

            List<Object> countObj = this.baseMapper.selectObjs(countWrapper);
            long triggerCount = 0;
            if (countObj != null && !countObj.isEmpty() && countObj.get(0) != null) {
                triggerCount = Long.parseLong(countObj.get(0).toString());
            }

            // Trigger logic
            if (triggerCount >= rule.getThresholdCount()) {
                // Check if an active warning already exists for this rule and class recently
                // (avoid spamming)
                QueryWrapper<WarningRecord> recordCheckWrapper = new QueryWrapper<>();
                recordCheckWrapper.eq("del_flag", 0)
                        .eq("rule_id", rule.getId())
                        .eq("class_id", newReport.getClassId())
                        .eq("status", 0); // Active (not processed)
                long existingCount = warningRecordService.count(recordCheckWrapper);

                if (existingCount == 0) {
                    // Create new Warning Record
                    WarningRecord record = new WarningRecord();
                    record.setRuleId(rule.getId());
                    record.setClassId(newReport.getClassId());
                    record.setDiseaseId(rule.getDiseaseId()); // Can be null
                    record.setWarningLevel(rule.getRiskLevel());
                    record.setTriggerCount((int) triggerCount);
                    record.setStatus(0);
                    record.setDelFlag(0);
                    record.setCreateTime(new Date());
                    record.setUpdateTime(new Date());

                    warningRecordService.save(record);

                    WarningRecordDetail detail = new WarningRecordDetail();
                    detail.setWarningRecordId(record.getId());
                    detail.setHealthReportId(newReport.getId());
                    detail.setUserId(newReport.getUserId());
                    detail.setDelFlag(0);
                    detail.setCreateTime(new Date());
                    detail.setUpdateTime(new Date());
                    warningRecordDetailService.save(detail);
                } else {
                    // If exists, update trigger count
                    WarningRecord existingRecord = warningRecordService.getOne(recordCheckWrapper.last("limit 1"));
                    if (existingRecord != null) {
                        // Check if this report is already detailed (to avoid duplicate count triggers)
                        QueryWrapper<WarningRecordDetail> detailCheck = new QueryWrapper<>();
                        detailCheck.eq("warning_record_id", existingRecord.getId())
                                .eq("health_report_id", newReport.getId());
                        if (warningRecordDetailService.count(detailCheck) == 0) {
                            if (existingRecord.getTriggerCount() < triggerCount) {
                                existingRecord.setTriggerCount((int) triggerCount);
                                existingRecord.setUpdateTime(new Date());
                                warningRecordService.updateById(existingRecord);
                            }

                            WarningRecordDetail detail = new WarningRecordDetail();
                            detail.setWarningRecordId(existingRecord.getId());
                            detail.setHealthReportId(newReport.getId());
                            detail.setUserId(newReport.getUserId());
                            detail.setDelFlag(0);
                            detail.setCreateTime(new Date());
                            detail.setUpdateTime(new Date());
                            warningRecordDetailService.save(detail);
                        }
                    }
                }
            }
        }
    }
}
