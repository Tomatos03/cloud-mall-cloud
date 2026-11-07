package com.cloudmall.search.index;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.cloudmall.search.properties.SearchProperties;

/**
 * 索引初始化器
 * <p>
 * 应用启动时幂等创建商品索引：索引已存在则跳过，
 * 不存在则按 {@code es/cm_product-mapping.json} 创建。
 * 注意：mapping 依赖 IK 分词插件（analyzer: ik_max_word / ik_smart），
 * 未安装插件时创建会失败并阻止启动。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexInitializer implements ApplicationRunner {

    private static final String MAPPING_RESOURCE = "es/cm_product-mapping.json";

    private final ElasticsearchClient client;
    private final SearchProperties props;

    /**
     * 启动时执行索引初始化。
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            createIndexIfAbsent();
        } catch (IOException e) {
            throw new IllegalStateException("初始化 ES 索引失败", e);
        }
    }

    private void createIndexIfAbsent() throws IOException {
        String index = props.getIndex();
        if (client.indices()
                  .exists(e -> e.index(index))
                  .value()) {
            log.info("ES 索引已存在，跳过创建: {}", index);
            return;
        }
        String mapping = loadMapping();
        client.indices().create(c -> c.index(index).withJson(new StringReader(mapping)));
        log.info("ES 索引创建完成: {}", index);
    }

    private String loadMapping() {
        try (InputStream in = new ClassPathResource(MAPPING_RESOURCE).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取 ES mapping 资源失败: " + MAPPING_RESOURCE, e);
        }
    }
}
