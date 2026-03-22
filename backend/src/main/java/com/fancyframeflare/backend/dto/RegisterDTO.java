package com.fancyframeflare.backend.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String studentNo;
    private String password;
    private String realName;
    private String phone;
}
