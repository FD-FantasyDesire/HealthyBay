package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("role_permission")
public class RolePermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long permissionId;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
