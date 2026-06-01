package com.example.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件（PaginationInnerInterceptor），数据库类型为 MySQL
 * 添加此配置后，MyBatis-Plus 的 Page 对象可自动解析分页参数并执行物理分页
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器
     * 添加分页插件，指定数据库类型为 MySQL
     * @return MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件：自动拦截 SQL 并拼接 LIMIT 子句
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
