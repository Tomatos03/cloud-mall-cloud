package com.cloudmall.agent.service;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 知识库服务接口
 * <p>
 * 提供文档的增删查功能，用于 RAG 检索增强生成
 *
 * @author Tomatos
 * @date 2026/7/22
 */
public interface IKnowledgeService {

    /**
     * 上传纯文本文档到知识库
     *
     * @param content  文档内容
     * @param metadata 文档元数据
     * @return 文档 ID
     */
    String uploadDocument(String content, Map<String, Object> metadata);

    /**
     * 批量上传文件到知识库（支持 TXT、PDF、Markdown、Word 等格式）
     *
     * @param files    上传的文件列表
     * @param category 文档分类（可选）
     * @return 文档 ID 列表
     * @throws IOException 文件读取或解析异常
     */
    List<String> uploadFiles(List<MultipartFile> files, String category) throws IOException;

    /**
     * 删除指定文档
     *
     * @param documentId 文档 ID
     */
    void deleteDocument(String documentId);

    /**
     * 根据查询搜索相似文档
     *
     * @param query 查询文本
     * @param topK  返回文档数量
     * @return 相似文档列表
     */
    List<Document> searchDocuments(String query, int topK);
}
