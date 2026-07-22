package com.cloudmall.agent.parser;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文档解析器接口
 * <p>
 * 每种文件格式对应一个实现，负责将上传文件解析为 Document 列表
 *
 * @author Tomatos
 * @date 2026/7/22
 */
public interface DocumentParser {

    /**
     * 解析上传文件并返回 Document 列表
     *
     * @param file 上传文件
     * @return Document 列表
     * @throws IOException 文件读取异常
     */
    List<Document> parse(MultipartFile file) throws IOException;

    /**
     * 返回支持的文档类型
     *
     * @return 文档类型
     */
    DocumentType getType();
}
