package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("warning_rule")
public class WarningRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private Long classScope;
    private Long diseaseId;
    private Integer thresholdCount;
    private Integer timeWindowHours;
    private Integer riskLevel;
    private Integer enabled;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
