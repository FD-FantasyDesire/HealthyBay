package com.fancyframeflare.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class WarningRecordQueryDTO {
    private Integer current = 1;
    private Integer size = 10;
    private Integer status;
    private Long classId;
    private List<Long> managerClassIds;
}
