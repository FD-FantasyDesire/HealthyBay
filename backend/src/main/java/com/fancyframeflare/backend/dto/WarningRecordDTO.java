package com.fancyframeflare.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class WarningRecordDTO {
    private Long id;
    private Long ruleId;
    private Long classId;
    private Long diseaseId;
    private Integer warningLevel;
    private Integer triggerCount;
    private Integer status;
}
