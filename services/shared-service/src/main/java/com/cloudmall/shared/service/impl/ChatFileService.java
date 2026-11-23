package com.cloudmall.shared.service.impl;

import java.util.Locale;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.shared.api.enums.ChatMediaTypeEnum;
import com.cloudmall.shared.api.enums.FileBizTypeEnum;
import com.cloudmall.shared.api.model.resp.ChatFileResp;
import com.cloudmall.shared.constant.FileConstants;
import com.cloudmall.shared.model.bo.FileUploadPolicy;
import com.cloudmall.shared.model.bo.FileUploadResult;
import com.cloudmall.shared.service.IChatFileService;
import com.cloudmall.shared.service.IFileService;

/**
 * 聊天附件服务实现。
 * <p>
 * 校验图片/视频类型与大小后，交由文件服务统一落对象存储并记入 cm_file 台账。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatFileService implements IChatFileService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov", "m4v");

    private final IFileService fileService;

    @Value("${cloudmall.file.chat.max-image-size:10485760}")
    private long maxImageSize;

    @Value("${cloudmall.file.chat.max-video-size:104857600}")
    private long maxVideoSize;

    @Override
    public ChatFileResp upload(MultipartFile file) {
        Asserts.notNull(file, BizErrorEnum.IM_FILE_EMPTY);
        Asserts.isFalse(file.isEmpty(), BizErrorEnum.IM_FILE_EMPTY);

        String originalName = StringUtils.cleanPath(
                StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file"
        );
        String extension = getExtension(originalName);
        ChatMediaTypeEnum type = resolveType(file.getContentType(), extension);
        Asserts.notNull(type, BizErrorEnum.IM_FILE_TYPE_NOT_ALLOWED);

        long maxSize = ChatMediaTypeEnum.IMAGE == type ? maxImageSize : maxVideoSize;
        FileUploadPolicy policy = new FileUploadPolicy(
                FileConstants.CHAT_KEY_PREFIX,
                FileConstants.ALLOWED_CHAT_EXTENSIONS,
                FileConstants.ALLOWED_CHAT_CONTENT_TYPES,
                maxSize
        );
        FileUploadResult result = fileService.upload(
                UserContextHolder.getUserId(),
                FileBizTypeEnum.CHAT,
                file,
                policy
        );
        return ChatFileResp.builder()
                           .id(result.id())
                           .url(result.url())
                           .type(type)
                           .name(originalName)
                           .size(file.getSize())
                           .build();
    }

    /**
     * 解析文件对应的媒体类型。
     *
     * @param contentType 浏览器上报的 MIME 类型
     * @param extension 文件扩展名
     * @return 图片或视频类型，不符合时返回 null
     */
    private ChatMediaTypeEnum resolveType(String contentType, String extension) {
        if (IMAGE_EXTENSIONS.contains(extension) && (contentType == null || contentType.startsWith("image/"))) {
            return ChatMediaTypeEnum.IMAGE;
        }
        if (VIDEO_EXTENSIONS.contains(extension) && (contentType == null || contentType.startsWith("video/"))) {
            return ChatMediaTypeEnum.VIDEO;
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
