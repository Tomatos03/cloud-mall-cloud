package com.cloudmall.outbox.relay.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.cloudmall.outbox.relay.job.OutboxRelayJob;
import com.cloudmall.outbox.relay.properties.OutboxRelayProperties;
import com.cloudmall.outbox.relay.service.IOutboxEventDispatcher;
import com.cloudmall.outbox.relay.service.OutboxRelay;
import com.cloudmall.outbox.relay.service.RocketMQOutboxEventDispatcher;
import com.cloudmall.outbox.repository.OutboxRepository;
import com.cloudmall.rocketmq.producer.MQProducer;

/**
 * Outbox Relay 自动装配。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@AutoConfiguration
@EnableConfigurationProperties(OutboxRelayProperties.class)
@ConditionalOnProperty(
        prefix = "cloudmall.outbox-relay",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxRelayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OutboxRelay outboxRelay(
        OutboxRepository outboxRepository,
        IOutboxEventDispatcher dispatcher,
        OutboxRelayProperties properties
    ) {
        return new OutboxRelay(outboxRepository, dispatcher, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxRelayJob outboxRelayJob(OutboxRelay outboxRelay) {
        return new OutboxRelayJob(outboxRelay);
    }

    @Bean
    @ConditionalOnMissingBean(IOutboxEventDispatcher.class)
    public IOutboxEventDispatcher rocketMQOutboxEventDispatcher(
        MQProducer mqProducer,
        ObjectMapper objectMapper,
        OutboxRelayProperties properties
    ) {
        return new RocketMQOutboxEventDispatcher(mqProducer, objectMapper, properties);
    }
}
