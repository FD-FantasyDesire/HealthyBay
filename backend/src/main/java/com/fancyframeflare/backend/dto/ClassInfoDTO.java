package com.fancyframeflare.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class ClassInfoDTO {
    private Long id;
    private String className;
    private String college;
    private String grade;
    private String major;
}
