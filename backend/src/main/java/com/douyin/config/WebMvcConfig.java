package com.douyin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 视频/图片等静态资源已迁移至 MinIO 对象存储，不再需要本地目录映射。
 * </p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
}
