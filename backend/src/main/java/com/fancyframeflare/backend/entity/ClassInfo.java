package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("class")
public class ClassInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private String college;
    private String grade;
    private String major;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
