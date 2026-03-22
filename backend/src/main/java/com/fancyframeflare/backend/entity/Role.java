package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleName;
    private String roleKey;
    private Integer status;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
