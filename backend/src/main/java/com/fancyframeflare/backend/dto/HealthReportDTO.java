package com.fancyframeflare.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class HealthReportDTO {
    private Long id;
    private Long userId;
    private Long classId;
    private Long roomId;
    private java.math.BigDecimal temperature;
    private String symptomJson;
    private Long diseaseId;
    private String exposureHistory;
    private Integer vaccinationStatus;
    private Integer riskLevel;
    private Date reportDate;
    private Integer status;
}
