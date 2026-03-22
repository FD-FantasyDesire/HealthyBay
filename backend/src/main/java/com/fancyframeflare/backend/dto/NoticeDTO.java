package com.fancyframeflare.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class NoticeDTO {
    private Long id;
    private String title;
    private String content;
    private Integer publishScope;
    private Long classId;
    private Long publisherId;
    private Integer status;
}
