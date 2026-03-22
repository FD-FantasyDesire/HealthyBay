package com.fancyframeflare.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class ManagerClassDTO {
    private Long id;
    private Long managerId;
    private Long classId;
}
