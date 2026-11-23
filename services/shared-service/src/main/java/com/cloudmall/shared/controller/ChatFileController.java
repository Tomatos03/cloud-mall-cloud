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

import com.cloudmall.common.model.Result;
import com.cloudmall.shared.model.FileContent;
import com.cloudmall.shared.model.resp.ChatFileResp;
import com.cloudmall.shared.service.IChatFileService;
import com.cloudmall.shared.service.IFileService;

/**
 * 聊天附件上传与访问接口。
 *
 * @author : Tomatos
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/files")
public class ChatFileController {

    private final IChatFileService chatFileService;
    private final IFileService fileService;

    /**
     * 上传聊天图片或视频。
     *
     * @param file 上传文件
     * @return 文件访问信息
     */
    @PostMapping
    public Result<ChatFileResp> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(chatFileService.upload(file));
    }

    /**
     * 读取聊天文件内容。
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
