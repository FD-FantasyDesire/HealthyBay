package com.fancyframeflare.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class WarningRuleDTO {
    private Long id;
    private String ruleName;
    private Long classScope;
    private Long diseaseId;
    private Integer thresholdCount;
    private Integer timeWindowHours;
    private Integer riskLevel;
    private Integer enabled;
}
