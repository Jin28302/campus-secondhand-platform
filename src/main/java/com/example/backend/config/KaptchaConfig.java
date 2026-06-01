package com.example.backend.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Kaptcha 验证码配置类
 * 配置图形验证码的生成参数：无边框、黑色字体、4位字符、120×40像素、无背景噪点
 */
@Configuration
public class KaptchaConfig {

    /**
     * 创建 Kaptcha 验证码生成器 Bean
     * 配置参数：
     * - 无边框
     * - 黑色字体，字号30
     * - 4位字符
     * - 图片宽度120px，高度40px
     * - 无背景噪点干扰
     * @return DefaultKaptcha 实例
     */
    @Bean
    public DefaultKaptcha kaptchaProducer() {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "no");                       // 无边框
        properties.setProperty("kaptcha.textproducer.font.color", "black");  // 字体颜色：黑色
        properties.setProperty("kaptcha.textproducer.char.length", "4");     // 验证码字符数量：4位
        properties.setProperty("kaptcha.image.width", "120");                // 图片宽度：120px
        properties.setProperty("kaptcha.image.height", "40");                // 图片高度：40px
        properties.setProperty("kaptcha.textproducer.font.size", "30");      // 字体大小：30
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise"); // 无噪点
        kaptcha.setConfig(new Config(properties));
        return kaptcha;
    }
}
