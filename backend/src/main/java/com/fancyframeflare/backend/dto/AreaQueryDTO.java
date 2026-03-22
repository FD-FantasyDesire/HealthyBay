package com.fancyframeflare.backend.dto;

import lombok.Data;

@Data
public class AreaQueryDTO {
    private Integer current = 1;
    private Integer size = 10;
    private String keyword;
    private Integer areaType;
    private Long parentId;
}
