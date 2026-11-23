package com.cloudmall.shared.storage.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.shared.storage.IFileStorage;

/**
 * 本地磁盘存储实现。
 * <p>
 * 仅用于开发联调与无法连接 MinIO 时的回滚，生产环境应使用对象存储。
 *
 * @author : Tomatos
 */
public class LocalFileStorage implements IFileStorage {

    private final Path baseDir;

    public LocalFileStorage(String baseDir) {
        this.baseDir = Paths.get(baseDir)
                            .toAbsolutePath()
                            .normalize();
    }

    @Override
    public void put(String objectKey, InputStream inputStream, long size, String contentType) {
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("写入本地文件失败: " + objectKey, e);
        }
    }

    @Override
    public InputStream open(String objectKey) {
        try {
            return Files.newInputStream(resolve(objectKey));
        } catch (IOException e) {
            throw new IllegalStateException("读取本地文件失败: " + objectKey, e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (IOException e) {
            throw new IllegalStateException("删除本地文件失败: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        return Files.exists(resolve(objectKey));
    }

    /**
     * 将对象 key 解析为安全的本地路径，防止路径穿越。
     *
     * @param objectKey 对象 key
     * @return 本地文件路径
     */
    private Path resolve(String objectKey) {
        Path path = baseDir.resolve(objectKey).normalize();
        Asserts.isTrue(path.startsWith(baseDir), BizErrorEnum.FILE_KEY_INVALID);
        return path;
    }
}
