package com.fancyframeflare.backend.dto;

import lombok.Data;

@Data
public class NoticeQueryDTO {
    private Integer current = 1;
    private Integer size = 10;
    private String keyword;
    private Integer status;
    private Long classId;
}
