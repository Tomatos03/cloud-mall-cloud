package com.cloudmall.shared.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.cloudmall.shared.enums.FileStorageTypeEnum;

/**
 * 文件服务配置。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "cloudmall.file")
public class FileProperties {

    /** 存储实现类型 */
    @Builder.Default
    private FileStorageTypeEnum storage = FileStorageTypeEnum.MINIO;

    /** 本地存储根目录（storage=LOCAL 时生效） */
    @Builder.Default
    private String localDir = "tmp/common-uploads";

    /** 清理任务配置 */
    @Builder.Default
    private Cleanup cleanup = new Cleanup();

    /** MinIO 连接配置 */
    @Builder.Default
    private Minio minio = new Minio();

    /**
     * 清理任务配置。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cleanup {

        /** 未绑定文件的保留秒数 */
        @Builder.Default
        private long pendingTtlSeconds = 86400;

        /** 孤儿文件的保留秒数 */
        @Builder.Default
        private long orphanTtlSeconds = 604800;
    }

    /**
     * MinIO 连接配置。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Minio {

        /** 服务地址 */
        @Builder.Default
        private String endpoint = "http://localhost:9010";

        /** 访问账号 */
        @Builder.Default
        private String accessKey = "cloudmall";

        /** 访问密钥 */
        @Builder.Default
        private String secretKey = "cloudmall123456";

        /** 存储桶名称 */
        @Builder.Default
        private String bucket = "cm-mall";
    }
}
