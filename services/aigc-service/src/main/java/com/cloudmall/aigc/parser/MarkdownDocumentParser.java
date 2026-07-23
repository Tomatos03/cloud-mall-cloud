package com.cloudmall.aigc.parser;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Markdown 文档解析器
 * <p>
 * 支持按标题级别和水平分割线将 Markdown 拆分为多个 Document
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@Component
public class MarkdownDocumentParser implements DocumentParser {

    /**
     * 解析 Markdown 文件
     *
     * @param file 上传文件
     * @return Document 列表
     * @throws IOException 文件读取异常
     */
    @Override
    public List<Document> parse(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig
                .builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(true)
                .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(content, config);
        List<Document> documents = reader.get();
        documents = documents.stream()
                             .map(doc -> doc
                                     .mutate()
                                     .id(UUID.randomUUID().toString())
                                     .build()
                             )
                             .toList();
        return documents;
    }

    /**
     * 返回 Markdown 类型
     *
     * @return MARKDOWN
     */
    @Override
    public DocumentType getType() {
        return DocumentType.MARKDOWN;
    }
}
