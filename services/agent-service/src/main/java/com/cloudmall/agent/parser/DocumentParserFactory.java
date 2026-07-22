package com.cloudmall.agent.parser;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文档解析器工厂
 * <p>
 * 根据文件类型自动选择对应的解析器，未知类型默认走 Tika
 *
 * @author Tomatos
 * @date 2026/7/22
 */
@Component
public class DocumentParserFactory {

    private final Map<DocumentType, DocumentParser> parserMap;

    public DocumentParserFactory(List<DocumentParser> parsers) {
        this.parserMap = parsers.stream()
                                .collect(Collectors.toMap(
                                        DocumentParser::getType,        // key
                                        Function.identity(),            // value
                                        (existing, replacement) -> replacement,  // 冲突处理
                                        () -> new EnumMap<>(DocumentType.class)  // 容器
                                ));
    }

    /**
     * 根据文件名获取对应的解析器
     *
     * @param filename 文件名
     * @return 文档解析器
     */
    public DocumentParser getParser(String filename) {
        DocumentType type = DocumentType.fromFilename(filename);
        return parserMap.getOrDefault(type, parserMap.get(DocumentType.Tika));
    }
}
