package com.fancyframeflare.backend.config;

import com.fancyframeflare.backend.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = parseJwt(request);
        if (token != null && jwtUtils.validateToken(token)) {
            Claims claims = jwtUtils.parseToken(token);
            String username = claims.getSubject();
            Long userId = claims.get("userId", Long.class);

            // 检查Redis中的Token状态
            String redisKey = "user:token:" + userId;
            String redisToken = stringRedisTemplate.opsForValue().get(redisKey);

            if (redisToken != null && redisToken.equals(token)) {
                // Token有效且存在于Redis中（未被覆盖/未过期）
                request.setAttribute("userId", userId);

                List<GrantedAuthority> authorities = new ArrayList<>();
                List<String> permissions = claims.get("permissions", List.class);
                if (permissions != null) {
                    for (String permission : permissions) {
                        if (permission != null && !permission.trim().isEmpty()) {
                            authorities.add(new SimpleGrantedAuthority(permission));
                        }
                    }
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                System.out.println("====== [Debug] Token已在Redis中失效或被顶替, userId=" + userId + " ======");
            }
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
