package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("health_report")
public class HealthReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long classId;
    private Long roomId;
    private java.math.BigDecimal temperature;
    private String symptomJson;
    private Long diseaseId;
    private String exposureHistory;
    private Integer vaccinationStatus;
    private Integer riskLevel;
    private Date reportDate;
    private Integer status;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
