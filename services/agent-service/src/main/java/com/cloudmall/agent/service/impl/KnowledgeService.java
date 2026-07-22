package com.cloudmall.agent.service.impl;

import com.cloudmall.agent.parser.DocumentParser;
import com.cloudmall.agent.parser.DocumentParserFactory;
import com.cloudmall.agent.service.IKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 知识库服务实现
 * <p>
 * 基于 Elasticsearch 向量存储实现文档的增删查功能
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@Service
@RequiredArgsConstructor
public class KnowledgeService implements IKnowledgeService {

    private final VectorStore vectorStore;
    private final DocumentParserFactory documentParserFactory;

    /**
     * 上传纯文本文档到知识库
     *
     * @param content  文档内容
     * @param metadata 文档元数据
     * @return 文档 ID
     */
    @Override
    public String uploadDocument(String content, Map<String, Object> metadata) {
        String documentId = UUID.randomUUID().toString();
        Document document = Document.builder()
                .id(documentId)
                .text(content)
                .metadata(metadata)
                .build();
        vectorStore.add(List.of(document));
        return documentId;
    }

    /**
     * 批量上传文件到知识库
     * <p>
     * 根据文件扩展名自动选择解析器，支持 TXT、PDF、Markdown、Word 等格式
     *
     * @param files    上传的文件列表
     * @param category 文档分类（可选）
     * @return 文档 ID 列表
     * @throws IOException 文件读取或解析异常
     */
    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String category) throws IOException {
        List<String> documentIds = new ArrayList<>();
        for (MultipartFile file : files) {
            DocumentParser parser = documentParserFactory.getParser(file.getOriginalFilename());
            List<Document> documents = parser.parse(file);

            documents.forEach(doc -> {
                doc.getMetadata().put("filename", file.getOriginalFilename());
                doc.getMetadata().put("size", file.getSize());
                if (category != null) {
                    doc.getMetadata().put("category", category);
                }
            });

            vectorStore.add(documents);
            documentIds.addAll(documents.stream().map(Document::getId).toList());
        }
        return documentIds;
    }

    /**
     * 删除指定文档
     *
     * @param documentId 文档 ID
     */
    @Override
    public void deleteDocument(String documentId) {
        vectorStore.delete(List.of(documentId));
    }

    /**
     * 根据查询搜索相似文档
     *
     * @param query 查询文本
     * @param topK  返回文档数量
     * @return 相似文档列表
     */
    @Override
    public List<Document> searchDocuments(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }
}
