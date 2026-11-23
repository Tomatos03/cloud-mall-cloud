package com.cloudmall.shared.constant;

import java.util.Set;

/**
 * 文件上传常量。
 *
 * @author : Tomatos
 */
public interface FileConstants {

    /** 通用图片 key 前缀 */
    String IMAGE_KEY_PREFIX = "files";

    /** 允许的图片扩展名 */
    Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    /** 允许的图片 MIME 前缀 */
    Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of("image/");

    /** 图片大小上限（5MB） */
    long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    /** 聊天文件 key 前缀 */
    String CHAT_KEY_PREFIX = "chat/files";

    /** 允许的聊天附件扩展名 */
    Set<String> ALLOWED_CHAT_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "gif",
            "webp",
            "mp4",
            "webm",
            "mov",
            "m4v"
    );

    /** 允许的聊天附件 MIME 前缀 */
    Set<String> ALLOWED_CHAT_CONTENT_TYPES = Set.of("image/", "video/");
}
