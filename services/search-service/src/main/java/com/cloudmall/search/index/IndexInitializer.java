package com.cloudmall.search.index;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cloudmall.search.properties.SearchProperties;

/**
 * 索引初始化器
 * <p>
 * 应用启动时幂等创建商品索引：索引已存在则跳过，
 * 不存在则按 {@code es/cm_product-mapping.json} 创建。
 * <p>
 * Elasticsearch 暂不可用时仅记录错误，不阻断应用启动，
 * 并由定时任务持续重试，待 ES 恢复后自动创建索引。
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
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean failureLogged = new AtomicBoolean(false);

    /**
     * 启动时执行索引初始化。
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        initializeIndex();
    }

    /**
     * ES 暂不可用时的重试入口。
     */
    @Scheduled(
            fixedDelayString = "${search.elasticsearch.init-retry-ms:60000}",
            initialDelayString = "${search.elasticsearch.init-retry-ms:60000}"
    )
    public void retryInitializeIndex() {
        if (!initialized.get()) {
            initializeIndex();
        }
    }

    /**
     * 初始化索引，失败时保留服务运行并等待下一次重试。
     */
    private void initializeIndex() {
        try {
            createIndexIfAbsent();
            initialized.set(true);
        } catch (Exception e) {
            if (failureLogged.compareAndSet(false, true)) {
                log.error("ES 索引初始化失败，search-service 将以降级状态运行并自动重试，ES={}", props.getUris(), e);
                return;
            }
            log.warn("ES 索引初始化重试失败，ES={}: {}", props.getUris(), e.getMessage());
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
