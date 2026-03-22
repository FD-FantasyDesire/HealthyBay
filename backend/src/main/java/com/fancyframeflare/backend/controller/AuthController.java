package com.fancyframeflare.backend.controller;

import com.fancyframeflare.backend.common.Result;
import com.fancyframeflare.backend.dto.LoginDTO;
import com.fancyframeflare.backend.dto.RegisterDTO;
import com.fancyframeflare.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        try {
            userService.register(dto);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        try {
            Map<String, Object> tokenData = userService.login(dto);
            return Result.success(tokenData);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
