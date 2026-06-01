package com.example.backend.common;

/**
 * 用户上下文工具类 - 基于 ThreadLocal 存储当前请求的用户信息
 *
 * 用途：
 * - 在请求处理过程中（如拦截器/过滤器设置后），各层代码可直接获取当前登录用户的信息
 * - 避免在方法参数中层层传递用户信息
 * - 请求结束后必须调用 remove() 清除，防止内存泄漏
 *
 * 使用示例：
 *   UserContext.set(new UserInfo(1L, "13800138000", "user"));
 *   UserInfo info = UserContext.get();
 *   UserContext.remove();
 */
public class UserContext {

    /** ThreadLocal 存储当前线程的用户信息，每个请求一个线程，天然隔离 */
    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    /**
     * 设置当前请求的用户信息
     * @param info 用户信息记录
     */
    public static void set(UserInfo info) {
        HOLDER.set(info);
    }

    /**
     * 获取当前请求的用户信息
     * @return 用户信息记录，可能为 null
     */
    public static UserInfo get() {
        return HOLDER.get();
    }

    /**
     * 清除 ThreadLocal，防止内存泄漏
     * 应在请求结束后（如拦截器的 afterCompletion）调用
     */
    public static void remove() {
        HOLDER.remove();
    }

    /**
     * 用户信息记录（Java 14+ record 类型，不可变数据类）
     * @param userId 用户ID
     * @param phone 手机号
     * @param role 角色：user/seller/admin
     */
    public record UserInfo(Long userId, String phone, String role) {}
}
