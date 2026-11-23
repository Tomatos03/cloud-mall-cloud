package com.cloudmall.shared.controller;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.common.model.Result;
import com.cloudmall.shared.api.enums.FileBizTypeEnum;
import com.cloudmall.shared.constant.FileConstants;
import com.cloudmall.shared.model.FileContent;
import com.cloudmall.shared.model.FileUploadPolicy;
import com.cloudmall.shared.model.FileUploadResult;
import com.cloudmall.shared.model.resp.FileUploadResp;
import com.cloudmall.shared.service.IFileService;

/**
 * 图片上传与访问接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private static final FileUploadPolicy IMAGE_POLICY = new FileUploadPolicy(
            FileConstants.IMAGE_KEY_PREFIX,
            FileConstants.ALLOWED_IMAGE_EXTENSIONS,
            FileConstants.ALLOWED_IMAGE_CONTENT_TYPES,
            FileConstants.MAX_IMAGE_SIZE
    );

    private final IFileService fileService;

    /**
     * 上传图片。
     *
     * @param file 图片文件
     * @return 文件 ID 与访问地址
     */
    @PostMapping("/upload")
    public Result<FileUploadResp> upload(@RequestParam("file") MultipartFile file) {
        FileUploadResult result = fileService.upload(
                UserContextHolder.getUserId(),
                FileBizTypeEnum.GENERAL,
                file,
                IMAGE_POLICY
        );
        return Result.success(FileUploadResp.builder()
                                            .id(result.id())
                                            .url(result.url())
                                            .build());
    }

    /**
     * 读取文件内容。
     *
     * @param request 当前请求
     * @return 文件流
     */
    @GetMapping("/**")
    public ResponseEntity<StreamingResponseBody> download(HttpServletRequest request) {
        String objectKey = resolveObjectKey(request.getRequestURI());
        FileContent content = fileService.requireContent(objectKey);
        StreamingResponseBody body = outputStream -> {
            try (InputStream inputStream = fileService.openObject(objectKey)) {
                inputStream.transferTo(outputStream);
            }
        };
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                                                           .contentType(
                                                                   MediaType.parseMediaType(
                                                                           content.contentType()
                                                                   )
                                                           );
        if (content.size() > 0) {
            builder.contentLength(content.size());
        }
        return builder.body(body);
    }

    /**
     * 从请求路径解析对象 key。
     *
     * @param requestUri 请求路径
     * @return 对象 key
     */
    private static String resolveObjectKey(String requestUri) {
        return requestUri.startsWith("/") ? requestUri.substring(1) : requestUri;
    }
}
