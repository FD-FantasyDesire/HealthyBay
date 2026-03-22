package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("manager_class")
public class ManagerClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long managerId;
    private Long classId;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
