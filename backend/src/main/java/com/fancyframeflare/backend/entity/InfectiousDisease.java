package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("infectious_disease")
public class InfectiousDisease {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String diseaseName;
    private String diseaseCode;
    private Integer severityLevel;
    private String incubationPeriod;
    private String symptoms;
    private String preventionMeasures;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
