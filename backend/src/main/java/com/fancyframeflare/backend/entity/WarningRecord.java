package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("warning_record")
public class WarningRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    private Long classId;
    private Long diseaseId;
    private Integer warningLevel;
    private Integer triggerCount;
    private Integer status;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
