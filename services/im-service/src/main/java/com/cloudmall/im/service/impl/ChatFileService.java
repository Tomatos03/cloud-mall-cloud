package com.cloudmall.im.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.im.enums.MessageTypeEnum;
import com.cloudmall.im.model.resp.ChatFileResp;
import com.cloudmall.im.service.IChatFileService;

/**
 * 聊天文件服务实现
 * <p>
 * 图片/视频保存到本地目录，通过静态资源映射对外提供访问。
 *
 * @author : Tomatos
 * @date : 2026/11
 */
@Slf4j
@Service
public class ChatFileService implements IChatFileService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov", "m4v");

    @Value("${im.upload-dir:tmp/im-uploads}")
    private String uploadDir;

    @Value("${im.max-image-size:10485760}")
    private long maxImageSize;

    @Value("${im.max-video-size:104857600}")
    private long maxVideoSize;

    @Override
    public ChatFileResp upload(MultipartFile file) {
        Asserts.notNull(file, BizErrorEnum.IM_FILE_EMPTY);
        Asserts.isFalse(file.isEmpty(), BizErrorEnum.IM_FILE_EMPTY);

        String originalName = StringUtils.cleanPath(
                StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file"
        );
        String extension = getExtension(originalName);
        MessageTypeEnum type = resolveType(file.getContentType(), extension);
        Asserts.notNull(type, BizErrorEnum.IM_FILE_TYPE_NOT_ALLOWED);

        long maxSize = MessageTypeEnum.IMAGE == type ? maxImageSize : maxVideoSize;
        Asserts.isTrue(file.getSize() <= maxSize, BizErrorEnum.IM_FILE_TOO_LARGE);

        String filename = IdUtil.fastSimpleUUID() + "." + extension;
        Path directory = Paths.get(uploadDir)
                              .toAbsolutePath()
                              .normalize();
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(filename).normalize();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("保存聊天文件失败: {}", originalName, e);
            throw new BizException(BizErrorEnum.IM_FILE_SAVE_FAILED);
        }

        return ChatFileResp.builder()
                           .url("/chat/files/" + filename)
                           .type(type)
                           .name(originalName)
                           .size(file.getSize())
                           .build();
    }

    /**
     * 解析文件对应的消息类型。
     *
     * @param contentType 浏览器上报的 MIME 类型
     * @param extension 文件扩展名
     * @return 图片或视频类型，不符合时返回 null
     */
    private MessageTypeEnum resolveType(String contentType, String extension) {
        if (IMAGE_EXTENSIONS.contains(extension) && (contentType == null || contentType.startsWith("image/"))) {
            return MessageTypeEnum.IMAGE;
        }
        if (VIDEO_EXTENSIONS.contains(extension) && (contentType == null || contentType.startsWith("video/"))) {
            return MessageTypeEnum.VIDEO;
        }
        return null;
    }

    /**
     * 获取小写扩展名。
     *
     * @param filename 文件名
     * @return 扩展名，不存在时返回空字符串
     */
    private String getExtension(String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        return extension == null ? "" : extension.toLowerCase(Locale.ROOT);
    }
}
