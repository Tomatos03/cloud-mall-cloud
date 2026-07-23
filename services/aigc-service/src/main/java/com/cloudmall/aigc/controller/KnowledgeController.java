package com.cloudmall.aigc.controller;

import com.cloudmall.aigc.model.req.KnowledgeUploadReq;
import com.cloudmall.aigc.service.IKnowledgeService;
import com.cloudmall.common.entity.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 知识库管理接口
 * <p>
 * 提供文档的上传、删除、搜索功能
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final IKnowledgeService knowledgeService;

    /**
     * 上传文档到知识库
     *
     * @param req 文档上传请求
     * @return 文档 ID
     */
    @PostMapping("/documents")
    public Result<String> uploadDocument(@Valid @RequestBody KnowledgeUploadReq req) {
        String documentId = knowledgeService.uploadDocument(req.getContent(), req.getMetadata());
        return Result.success(documentId);
    }

    /**
     * 批量上传文件到知识库
     * <p>
     * 支持 TXT、PDF、Markdown、Word、Excel、HTML 等格式，
     * 根据文件扩展名自动选择对应的解析器
     *
     * @param files    上传的文件列表
     * @param category 文档分类（可选）
     * @return 文档 ID 列表
     * @throws IOException 文件读取或解析异常
     */
    @PostMapping("/documents/file")
    public Result<List<String>> uploadFile(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "category", required = false) String category
    ) throws IOException {
        return Result.success(knowledgeService.uploadFiles(files, category));
    }

    /**
     * 删除指定文档
     *
     * @param id 文档 ID
     * @return 操作结果
     */
    @DeleteMapping("/documents/{id}")
    public Result<Void> deleteDocument(@PathVariable String id) {
        knowledgeService.deleteDocument(id);
        return Result.success();
    }

    /**
     * 搜索相似文档
     *
     * @param query 查询文本
     * @param topK  返回文档数量（默认 3）
     * @return 相似文档列表
     */
    @GetMapping("/search")
    public Result<List<Document>> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int topK
    ) {
        return Result.success(knowledgeService.searchDocuments(query, topK));
    }
}
