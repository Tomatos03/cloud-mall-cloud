package com.cloudmall.feign.autoconfigure;

import java.beans.Introspector;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * Feign 兜底工厂自动注册。
 * <p>
 * 开启 {@code feign.sentinel.enabled} 后，SentinelFeign 会从每个 Feign 客户端的 Spring 子上下文按类型查找
 * {@code fallbackFactory} bean（{@code SentinelFeign.Builder#getFromContext}）。兜底工厂类位于 service-api 模块，
 * 不在业务服务的组件扫描范围内，若不注册为 bean，Feign 客户端创建时即抛
 * {@code No fallbackFactory instance of type ... found for feign client ...}，服务无法启动。
 * 这里在启动时扫描 classpath 下所有 {@link FallbackFactory} 实现并注册为 bean：starter 无需依赖任何 service-api
 * 模块，新增兜底工厂也无需修改此处；已存在同名 bean（如服务自行声明）时跳过，避免覆盖。
 *
 * @author : Tomatos
 */
@AutoConfiguration
@Import(FeignFallbackAutoConfiguration.FallbackFactoryRegistrar.class)
public class FeignFallbackAutoConfiguration {

    /**
     * 按类型扫描并注册 FallbackFactory 实现。
     */
    public static class FallbackFactoryRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

        private static final String BASE_PACKAGE = "com.cloudmall";

        private ResourceLoader resourceLoader;

        private Environment environment;

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void registerBeanDefinitions(
            AnnotationMetadata importingClassMetadata,
            BeanDefinitionRegistry registry
        ) {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                    false,
                    environment
            );
            scanner.setResourceLoader(resourceLoader);
            scanner.addIncludeFilter(new AssignableTypeFilter(FallbackFactory.class));

            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(BASE_PACKAGE)) {
                String className = beanDefinition.getBeanClassName();
                String beanName = Introspector.decapitalize(ClassUtils.getShortName(className));
                if (!registry.containsBeanDefinition(beanName)) {
                    registry.registerBeanDefinition(beanName, beanDefinition);
                }
            }
        }
    }
}
