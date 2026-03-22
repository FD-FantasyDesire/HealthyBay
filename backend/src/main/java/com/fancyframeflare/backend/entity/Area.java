package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("area")
public class Area {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String areaName;
    private Integer areaType;
    private Long parentId;
    private Integer level;
    private String areaCode;
    private Long managerId; // 楼栋/房间管理员ID
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
