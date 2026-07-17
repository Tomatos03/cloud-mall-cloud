package com.cloudmall.mybatisplus.config;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Properties;

@Component
@Profile({"dev", "test"})
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class SqlLoggingInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SqlLoggingInterceptor.class);
    private static final long SLOW_THRESHOLD_MS = 1000;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            if (duration > SLOW_THRESHOLD_MS) {
                StatementHandler handler = (StatementHandler) invocation.getTarget();
                log.warn("Slow SQL ({}ms): {}", duration, handler.getBoundSql().getSql());
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
