package com.fancyframeflare.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fancyframeflare.backend.dto.LoginDTO;
import com.fancyframeflare.backend.dto.RegisterDTO;
import com.fancyframeflare.backend.entity.User;
import java.util.Map;

public interface UserService extends IService<User> {
    void register(RegisterDTO dto);

    Map<String, Object> login(LoginDTO dto);
}
