package com.fancyframeflare.backend.dto;

import lombok.Data;

@Data
public class OperationLogQueryDTO {
    private Integer current = 1;
    private Integer size = 10;
    private String keyword;
}
