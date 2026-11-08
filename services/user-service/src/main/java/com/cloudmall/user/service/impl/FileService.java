package com.cloudmall.user.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.service.IFileService;

/**
 * 本地文件上传服务。
 * <p>
 * 当前实现将图片保存到本地目录，返回 /files/{文件名} 访问路径；
 * 后续接入 MinIO/OSS 时只需替换本实现。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
public class FileService implements IFileService {

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    /** 单文件大小上限（5MB） */
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    /** 上传目录 */
    @Value("${cloudmall.upload.dir:tmp/uploads}")
    private String uploadDir;

    /**
     * 保存图片并返回访问路径。
     *
     * @param file 图片文件
     * @return 访问路径
     */
    @Override
    public String uploadImage(MultipartFile file) {
        Asserts.isFalse(file == null || file.isEmpty(), BizErrorEnum.FILE_EMPTY);
        Asserts.isFalse(file.getSize() > MAX_FILE_SIZE, BizErrorEnum.FILE_SIZE_EXCEEDS_LIMIT);
        String extension = resolveExtension(file.getOriginalFilename());
        Asserts.contains(ALLOWED_EXTENSIONS, extension, BizErrorEnum.FILE_EXTENSION_NOT_ALLOWED);
        Asserts.isTrue(
                file.getContentType() != null && file.getContentType().startsWith("image/"),
                BizErrorEnum.FILE_MIME_TYPE_INVALID
        );
        String filename = UUID.randomUUID()
                              .toString()
                              .replace("-", "") + "." + extension;
        Path directory = Paths.get(uploadDir)
                              .toAbsolutePath()
                              .normalize();
        Path target = directory.resolve(filename);
        try {
            Files.createDirectories(directory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("保存上传文件失败 filename={}", filename, e);
            throw new BizException(BizErrorEnum.FILE_SAVE_FAILED);
        }
        return "/files/" + filename;
    }

    /**
     * 解析文件扩展名。
     *
     * @param originalFilename 原始文件名
     * @return 小写扩展名
     */
    private static String resolveExtension(String originalFilename) {
        if (StrUtil.isBlank(originalFilename)) {
            return "";
        }
        String extension = StrUtil.subAfter(originalFilename, ".", true);
        return extension.toLowerCase(Locale.ROOT);
    }
}
