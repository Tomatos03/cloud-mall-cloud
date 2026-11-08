package com.cloudmall.user.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 上传文件静态资源映射。
 *
 * @author : Tomatos
 */
@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    /** 上传目录 */
    @Value("${cloudmall.upload.dir:tmp/uploads}")
    private String uploadDir;

    /**
     * 将 /files/** 映射到本地上传目录。
     *
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path directory = Paths.get(uploadDir)
                              .toAbsolutePath()
                              .normalize();
        String location = directory.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/files/**").addResourceLocations(location);
    }
}
