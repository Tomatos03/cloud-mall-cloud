package com.cloudmall.agent.parser;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 通用文档解析器
 * <p>
 * 基于 Apache Tika，支持 Word、Excel、PowerPoint、HTML 等格式
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@Component
public class TikaDocumentParser implements DocumentParser {

    /**
     * 使用 Tika 解析文件
     *
     * @param file 上传文件
     * @return Document 列表
     * @throws IOException 文件读取异常
     */
    @Override
    public List<Document> parse(MultipartFile file) throws IOException {
        InputStreamResource resource = new InputStreamResource(file.getInputStream());
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        documents = documents.stream().map(doc ->
                doc.mutate().id(UUID.randomUUID().toString()).build()
        ).toList();
        return documents;
    }

    /**
     * 返回通用格式类型
     *
     * @return Tika
     */
    @Override
    public DocumentType getType() {
        return DocumentType.Tika;
    }
}
