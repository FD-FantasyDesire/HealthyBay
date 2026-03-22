package com.fancyframeflare.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class OperationLogDTO {
    private Long id;
    private Long userId;
    private String operation;
    private String method;
    private String requestIp;
    private Integer status;
    private String errorMsg;
}
