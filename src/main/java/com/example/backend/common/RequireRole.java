package com.example.backend.common;

import java.lang.annotation.*;

/**
 * 角色权限注解 - 标记在 Controller 方法上，限制只有指定角色才能访问
 *
 * 使用示例：
 *   @RequireRole({"admin"})            // 仅管理员可访问
 *   @RequireRole({"seller", "admin"})  // 商家和管理员可访问
 *
 * 该注解配合 AOP 切面或拦截器使用，在方法执行前检查当前用户的角色是否在允许列表中
 */
@Target(ElementType.METHOD)           // 注解目标：方法
@Retention(RetentionPolicy.RUNTIME)   // 注解保留期：运行时（反射可读取）
@Documented                           // 包含在 Javadoc 中
public @interface RequireRole {
    /** 允许访问的角色列表（字符串数组） */
    String[] value();
}
