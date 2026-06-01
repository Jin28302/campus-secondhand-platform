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

// 认证拦截器 - 在请求进入Controller前校验JWT token和角色权限
@Component
public class AuthInterceptor implements HandlerInterceptor {

    // 请求前拦截 - 解析Authorization头中的Bearer token，验证登录状态和角色权限
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非HandlerMethod（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 从请求头提取Bearer token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 检查方法上是否有RequireRole注解
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        // 未携带token时：若接口需要权限则拒绝，否则放行
        if (token == null || token.isBlank()) {
            if (requireRole != null) {
                throw new BusinessException(401, "未登录");
            }
            return true;
        }

        // 解析JWT token，提取用户ID、手机号、角色
        Claims claims;
        try {
            claims = JwtUtil.parse(token);
        } catch (Exception e) {
            // token无效时：若接口需要权限则拒绝，否则放行
            if (requireRole != null) {
                throw new BusinessException(401, "token无效或已过期");
            }
            return true;
        }

        Long userId = Long.valueOf(claims.getSubject());
        String phone = claims.get("phone", String.class);
        String role = claims.get("role", String.class);

        // 将用户信息存入ThreadLocal，供后续业务代码使用
        UserContext.set(new UserContext.UserInfo(userId, phone, role));

        // 校验角色权限：用户角色必须在允许的角色列表中
        if (requireRole != null) {
            boolean hasRole = Arrays.asList(requireRole.value()).contains(role);
            if (!hasRole) {
                throw new BusinessException(403, "权限不足");
            }
        }

        return true;
    }

    // 请求结束后清理ThreadLocal中的用户信息，防止内存泄漏
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }
}
