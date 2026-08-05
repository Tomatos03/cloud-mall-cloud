package com.cloudmall.xxljob.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * XXL-JOB 执行器与自动注册配置。
 *
 * @author : Tomatos
 */
@Data
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    /** 是否启用执行器，false 时不装配执行器与自动注册 */
    private boolean enabled = true;

    /** 调度中心配置 */
    private Admin admin = new Admin();

    /** 执行器与调度中心通信令牌，两端必须一致 */
    private String accessToken = "";

    /** 执行器配置 */
    private Executor executor = new Executor();

    /** 启动自动注册任务配置 */
    private AutoRegister autoRegister = new AutoRegister();

    @Data
    public static class Admin {

        /** 调度中心地址，多个逗号分隔 */
        private String addresses = "";

        /** 调度中心登录账号（自动注册使用） */
        private String username = "admin";

        /** 调度中心登录密码（自动注册使用） */
        private String password = "123456";
    }

    @Data
    public static class Executor {

        /** 执行器 AppName，与调度中心执行器配置对应 */
        private String appName = "";

        /** 执行器注册 IP，默认自动探测 */
        private String ip = "";

        /** 执行器端口，默认 9999 */
        private int port = 9999;

        /** 执行器日志目录 */
        private String logPath = "/tmp/xxl-job/jobhandler";

        /** 执行器日志保留天数 */
        private int logRetentionDays = 30;
    }

    @Data
    public static class AutoRegister {

        /** 是否启动时自动注册任务 */
        private boolean enabled = false;

        /** 任务定义列表 */
        private List<JobDef> jobs = new ArrayList<>();
    }

    /**
     * 任务定义（声明式，key 为 @XxlJob handler 名）。
     */
    @Data
    public static class JobDef {

        /** 执行器任务 Handler 名称，与 @XxlJob 注解 value 一致 */
        private String handler;

        /** 任务描述 */
        private String desc;

        /** 调度类型：CRON / FIX_RATE / FIX_DELAY */
        private String scheduleType = "FIX_DELAY";

        /** 调度配置：CRON 表达式或秒数 */
        private String scheduleConf = "60";

        /** 调度过期策略：DO_NOTHING / FIRE_ONCE_NOW */
        private String misfireStrategy = "DO_NOTHING";

        /** 路由策略：FIRST / ROUND / FAILOVER / SHARDING_BROADCAST 等 */
        private String executorRouteStrategy = "FIRST";

        /** 任务参数 */
        private String executorParam = "";

        /** 阻塞处理策略：SERIAL_EXECUTION / DISCARD_LATER / COVER_EARLY */
        private String executorBlockStrategy = "SERIAL_EXECUTION";

        /** 任务执行超时时间（秒），0 表示不限制 */
        private int executorTimeout = 0;

        /** 失败重试次数 */
        private int executorFailRetryCount = 0;

        /** 负责人 */
        private String author = "cloudmall";

        /** 报警邮件，多个逗号分隔 */
        private String alarmEmail = "";
    }
}
