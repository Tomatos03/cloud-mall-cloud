package com.cloudmall.shared.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudmall.shared.properties.FileProperties;
import com.cloudmall.shared.storage.IFileStorage;
import com.cloudmall.shared.storage.impl.LocalFileStorage;
import com.cloudmall.shared.storage.impl.MinioFileStorage;

/**
 * 文件存储配置。
 * <p>
 * 根据 {@code cloudmall.file.storage} 选择本地磁盘或 MinIO 实现。
 *
 * @author : Tomatos
 */
@Configuration
@EnableConfigurationProperties(FileProperties.class)
public class FileConfig {

    /**
     * 创建存储实现。
     *
     * @param properties 文件服务配置
     * @return 存储实现
     */
    @Bean
    @ConditionalOnMissingBean
    public IFileStorage fileStorage(FileProperties properties) {
        return switch (properties.getStorage()) {
            case LOCAL -> new LocalFileStorage(properties.getLocalDir());
            case MINIO -> new MinioFileStorage(
                    buildMinioClient(properties.getMinio()),
                    properties.getMinio().getBucket()
            );
        };
    }

    /**
     * 构建 MinIO 客户端。
     *
     * @param minio MinIO 配置
     * @return MinIO 客户端
     */
    private static MinioClient buildMinioClient(FileProperties.Minio minio) {
        return MinioClient.builder()
                          .endpoint(minio.getEndpoint())
                          .credentials(minio.getAccessKey(), minio.getSecretKey())
                          .build();
    }
}
