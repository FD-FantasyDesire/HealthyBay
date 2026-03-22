package com.fancyframeflare.backend.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class AreaDTO {
    private Long id;
    private String areaName;
    private Integer areaType;
    private Long parentId;
    private Integer level;
    private String areaCode;
    private List<AreaDTO> children;
}
