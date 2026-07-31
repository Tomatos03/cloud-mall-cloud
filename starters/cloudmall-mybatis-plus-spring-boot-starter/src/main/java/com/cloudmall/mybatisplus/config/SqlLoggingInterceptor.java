package com.cloudmall.mybatisplus.config;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class SqlLoggingInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SqlLoggingInterceptor.class);
    private static final long SLOW_THRESHOLD_MS = 1000;

    /**
     * 拦截并处理调用。
     *
     * @param invocation 拦截器调用信息
     * @return 处理结果
     */
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

    /**
     * 为目标对象创建插件代理。
     *
     * @param target 目标对象
     * @return 处理结果
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 更新相关数据。
     *
     * @param properties 配置属性
     */
    @Override
    public void setProperties(Properties properties) {
    }
}
