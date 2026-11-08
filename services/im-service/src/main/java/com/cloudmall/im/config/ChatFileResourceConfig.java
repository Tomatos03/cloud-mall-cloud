package com.cloudmall.im.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 聊天文件静态资源映射
 *
 * @author : Tomatos
 * @date : 2026/11
 */
@Configuration
public class ChatFileResourceConfig implements WebMvcConfigurer {

    @Value("${im.upload-dir:tmp/im-uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path directory = Paths.get(uploadDir)
                              .toAbsolutePath()
                              .normalize();
        String location = directory.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/chat/files/**").addResourceLocations(location);
    }
}
