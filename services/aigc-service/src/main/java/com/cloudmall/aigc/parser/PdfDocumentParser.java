package com.cloudmall.aigc.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * PDF 文档解析器
 * <p>
 * 使用 Spring AI 的 PagePdfDocumentReader 按页解析 PDF 文件
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@Component
public class PdfDocumentParser implements DocumentParser {

    /**
     * 解析输入内容。
     *
     * @param file 上传文件
     * @return 数据列表
     */
    @Override
    public List<Document> parse(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("upload-", ".pdf");
        try {
            file.transferTo(tempFile);
            PagePdfDocumentReader reader = new PagePdfDocumentReader(
                    new FileSystemResource(tempFile),
                    PdfDocumentReaderConfig.builder().build()
            );
            List<Document> documents = reader.get();
            documents = documents.stream()
                                 .map(doc -> doc
                                         .mutate()
                                         .id(UUID.randomUUID().toString())
                                         .build()
                                 )
                                 .toList();
            return documents;
        } finally {
            Files.deleteIfExists(Path.of(tempFile.getPath()));
        }
    }

    /**
     * 获取相关数据。
     *
     * @return 处理结果
     */
    @Override
    public DocumentType getType() {
        return DocumentType.PDF;
    }
}
