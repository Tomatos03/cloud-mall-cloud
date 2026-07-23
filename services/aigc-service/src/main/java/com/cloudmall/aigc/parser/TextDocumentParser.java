package com.cloudmall.aigc.parser;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 纯文本文档解析器
 * <p>
 * 支持 txt、csv、json、xml、yaml 等纯文本格式
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@Component
public class TextDocumentParser implements DocumentParser {

    /**
     * 解析纯文本文件
     *
     * @param file 上传文件
     * @return 包含单个 Document 的列表
     * @throws IOException 文件读取异常
     */
    @Override
    public List<Document> parse(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String documentId = UUID.randomUUID().toString();
        return List.of(Document.builder().id(documentId).text(content).build());
    }

    /**
     * 返回纯文本类型
     *
     * @return TEXT
     */
    @Override
    public DocumentType getType() {
        return DocumentType.TEXT;
    }
}
