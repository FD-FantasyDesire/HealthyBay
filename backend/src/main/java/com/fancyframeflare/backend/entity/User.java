package com.fancyframeflare.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String studentNo;
    private String password;
    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private Long classId;
    private Long roomId;
    private Integer status;
    private Integer delFlag;
    private Date createTime;
    private Date updateTime;
}
