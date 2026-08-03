package com.cloudmall.mybatisplus.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.cloudmall.mybatisplus.config.MyMetaObjectHandler;

@AutoConfiguration
@Import(MyMetaObjectHandler.class)
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class CloudmallMybatisPlusAutoConfiguration {

    /**
     * 创建 MyBatis-Plus 拦截器。
     *
     * @return 处理结果
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor();
        pagination.setMaxLimit(500L);
        pagination.setOverflow(true);
        pagination.setOptimizeJoin(true);
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
