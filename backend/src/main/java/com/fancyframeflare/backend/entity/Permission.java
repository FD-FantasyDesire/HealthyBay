package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String permName;
    private String permKey;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
