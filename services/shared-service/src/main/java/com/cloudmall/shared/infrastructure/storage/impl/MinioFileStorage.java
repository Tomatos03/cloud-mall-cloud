package com.cloudmall.shared.infrastructure.storage.impl;

import java.io.InputStream;

import cn.hutool.core.util.StrUtil;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;

import com.cloudmall.shared.infrastructure.storage.IFileStorage;

/**
 * MinIO 对象存储实现。
 *
 * @author : Tomatos
 */
@Slf4j
public class MinioFileStorage implements IFileStorage {

    private static final String NO_SUCH_KEY = "NoSuchKey";

    private final MinioClient client;
    private final String bucket;

    public MinioFileStorage(MinioClient client, String bucket) {
        this.client = client;
        this.bucket = bucket;
        ensureBucket();
    }

    @Override
    public void put(String objectKey, InputStream inputStream, long size, String contentType) {
        PutObjectArgs.Builder builder = PutObjectArgs.builder()
                                                     .bucket(bucket)
                                                     .object(objectKey)
                                                     .stream(inputStream, size, -1);
        if (StrUtil.isNotBlank(contentType)) {
            builder.contentType(contentType);
        }
        try {
            client.putObject(builder.build());
        } catch (Exception e) {
            throw new IllegalStateException("写入 MinIO 对象失败: " + objectKey, e);
        }
    }

    @Override
    public InputStream open(String objectKey) {
        try {
            return client.getObject(GetObjectArgs.builder()
                                                 .bucket(bucket)
                                                 .object(objectKey)
                                                 .build());
        } catch (Exception e) {
            throw new IllegalStateException("读取 MinIO 对象失败: " + objectKey, e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                                                .bucket(bucket)
                                                .object(objectKey)
                                                .build());
        } catch (ErrorResponseException e) {
            if (!isNoSuchKey(e)) {
                throw new IllegalStateException("删除 MinIO 对象失败: " + objectKey, e);
            }
        } catch (Exception e) {
            throw new IllegalStateException("删除 MinIO 对象失败: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            client.statObject(StatObjectArgs.builder()
                                            .bucket(bucket)
                                            .object(objectKey)
                                            .build());
            return true;
        } catch (ErrorResponseException e) {
            if (isNoSuchKey(e)) {
                return false;
            }
            throw new IllegalStateException("查询 MinIO 对象失败: " + objectKey, e);
        } catch (Exception e) {
            throw new IllegalStateException("查询 MinIO 对象失败: " + objectKey, e);
        }
    }

    /**
     * 确保存储桶存在，失败只记录日志，避免影响应用启动。
     */
    private void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder()
                                                                 .bucket(bucket)
                                                                 .build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder()
                                                .bucket(bucket)
                                                .build());
                log.info("已创建 MinIO 存储桶 bucket={}", bucket);
            }
        } catch (Exception e) {
            log.error("初始化 MinIO 存储桶失败 bucket={}", bucket, e);
        }
    }

    /**
     * 判断异常是否为对象不存在。
     *
     * @param e MinIO 错误响应异常
     * @return true 表示对象不存在
     */
    private static boolean isNoSuchKey(ErrorResponseException e) {
        return e.errorResponse() != null && NO_SUCH_KEY.equals(e.errorResponse().code());
    }
}
