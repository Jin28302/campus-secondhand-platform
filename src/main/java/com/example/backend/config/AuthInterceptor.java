package com.example.backend.config;

import com.example.backend.common.JwtUtil;
import com.example.backend.common.RequireRole;
import com.example.backend.common.UserContext;
import com.example.backend.exception.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        if (token == null || token.isBlank()) {
            if (requireRole != null) {
                throw new BusinessException(401, "未登录");
            }
            return true;
        }

        Claims claims;
        try {
            claims = JwtUtil.parse(token);
        } catch (Exception e) {
            if (requireRole != null) {
                throw new BusinessException(401, "token无效或已过期");
            }
            return true;
        }

        Long userId = Long.valueOf(claims.getSubject());
        String phone = claims.get("phone", String.class);
        String role = claims.get("role", String.class);

        UserContext.set(new UserContext.UserInfo(userId, phone, role));

        if (requireRole != null) {
            boolean hasRole = Arrays.asList(requireRole.value()).contains(role);
            if (!hasRole) {
                throw new BusinessException(403, "权限不足");
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }
}
