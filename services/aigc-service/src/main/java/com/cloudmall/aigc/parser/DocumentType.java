package com.cloudmall.aigc.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 文档类型枚举
 * <p>
 * 定义支持的文件类型及其对应的扩展名
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@Getter
@AllArgsConstructor
public enum DocumentType {

    TEXT("txt,csv,json,xml,yaml,yml,properties", "纯文本"),
    MARKDOWN("md", "Markdown"),
    PDF("pdf", "PDF 文档"),
    Tika("docx,xlsx,pptx,html,htm,doc,xls,ppt", "通用格式");

    private final String extensions;
    private final String description;

    /**
     * 根据文件名获取文档类型
     *
     * @param filename 文件名
     * @return 文档类型，未知扩展名默认返回 Tika
     */
    public static DocumentType fromFilename(String filename) {
        if (filename == null || !filename.contains(".")) {
            return Tika;
        }
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return Arrays.stream(values())
                     .filter(type -> Arrays.asList(type.getExtensions().split(",")).contains(ext))
                     .findFirst()
                     .orElse(Tika);
    }
}
