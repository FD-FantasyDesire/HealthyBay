package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("warning_record_detail")
public class WarningRecordDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long warningRecordId;
    private Long healthReportId;
    private Long userId;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
