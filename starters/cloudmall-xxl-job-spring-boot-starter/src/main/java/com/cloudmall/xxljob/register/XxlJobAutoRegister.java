package com.cloudmall.xxljob.register;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

import com.cloudmall.xxljob.config.XxlJobProperties;

/**
 * 启动时向调度中心自动注册任务（幂等）。
 * <p>
 * 任务定义在 {@code xxl.job.auto-register.jobs} 配置中声明，按 handler 名对齐
 * {@code @XxlJob} 注解。注册流程：登录调度中心 → 确保执行器组存在 → 查询任务
 * 是否存在 → 缺失则新增并启动。重复执行不产生副作用。
 *
 * @author : Tomatos
 */
@Slf4j
public class XxlJobAutoRegister implements ApplicationRunner, Ordered {

    private static final int SUCCESS_CODE = 200;
    private static final int REQUEST_TIMEOUT = 5_000;

    private static final String PATH_LOGIN = "/auth/doLogin";
    private static final String PATH_GROUP_PAGE = "/jobgroup/pageList";
    private static final String PATH_GROUP_INSERT = "/jobgroup/insert";
    private static final String PATH_JOB_PAGE = "/jobinfo/pageList";
    private static final String PATH_JOB_INSERT = "/jobinfo/insert";
    private static final String PATH_JOB_START = "/jobinfo/start";

    private final XxlJobProperties properties;
    private final String adminUrl;

    /** 登录会话 Cookie（XXL_JOB_LOGIN_IDENTITY=...） */
    private String cookie;

    public XxlJobAutoRegister(XxlJobProperties properties) {
        this.properties = properties;
        String addresses = properties.getAdmin().getAddresses();
        this.adminUrl = StrUtil.isBlank(addresses)
                ? ""
                : StrUtil.removeSuffix(StrUtil.split(addresses, ',')
                                              .get(0)
                                              .trim(), "/");
    }

    @Override
    public void run(ApplicationArguments args) {
        XxlJobProperties.AutoRegister autoRegister = properties.getAutoRegister();
        if (!autoRegister.isEnabled() || CollUtil.isEmpty(autoRegister.getJobs())) {
            return;
        }
        try {
            login();
            for (XxlJobProperties.JobDef job : autoRegister.getJobs()) {
                registerJob(job);
            }
            log.info("xxl-job 自动注册完成，共 {} 个任务", autoRegister.getJobs().size());
        } catch (Exception e) {
            log.error("xxl-job 自动注册失败，请确认调度中心可达或手动在管理台注册", e);
        }
    }

    /**
     * 登录调度中心，缓存会话 Cookie。
     */
    private void login() {
        try (HttpResponse loginResp = HttpRequest.post(adminUrl + PATH_LOGIN)
                                                 .timeout(REQUEST_TIMEOUT)
                                                 .form("userName", properties.getAdmin().getUsername())
                                                 .form("password", properties.getAdmin().getPassword())
                                                 .form("ifRemember", "on")
                                                 .execute()) {
            checkResponse(loginResp.body());
            this.cookie = loginResp.getCookieStr();
        }
        if (StrUtil.isBlank(cookie)) {
            throw new IllegalStateException("xxl-job 登录未返回会话 Cookie");
        }
        log.info("xxl-job 登录成功，admin={}", adminUrl);
    }

    /**
     * 注册单个任务：确保执行器组存在 → 任务缺失则新增并启动。
     *
     * @param job 任务定义
     */
    private void registerJob(XxlJobProperties.JobDef job) {
        if (StrUtil.isBlank(job.getHandler())) {
            log.warn("跳过无 handler 的任务定义: {}", job.getDesc());
            return;
        }
        String appName = properties.getExecutor().getAppName();
        int groupId = ensureGroup(appName);

        JSONObject exist = findJob(groupId, job.getHandler());
        if (exist != null) {
            log.info("任务已存在，跳过注册: handler={}, jobId={}", job.getHandler(), exist.getInt("id"));
            return;
        }
        addJob(groupId, job);
        JSONObject created = findJob(groupId, job.getHandler());
        if (created != null && created.getInt("triggerStatus", 0) != 1) {
            startJob(created.getInt("id"));
        }
        log.info(
                "任务注册并启动成功: handler={}, jobId={}",
                job.getHandler(),
                created == null ? "-" : created.getInt("id")
        );
    }

    /**
     * 确保执行器组存在，返回组 ID。
     *
     * @param appName 执行器 AppName
     * @return 执行器组 ID
     */
    private int ensureGroup(String appName) {
        JSONObject group = findGroup(appName);
        if (group != null) {
            return group.getInt("id");
        }
        String body = HttpRequest.post(adminUrl + PATH_GROUP_INSERT)
                                 .timeout(REQUEST_TIMEOUT)
                                 .header("Cookie", cookie)
                                 .form("appname", appName)
                                 .form("title", appName)
                                 .form("addressType", 0)
                                 .execute()
                                 .body();
        checkResponse(body);
        JSONObject created = findGroup(appName);
        if (created == null) {
            throw new IllegalStateException("执行器组创建后查询不到: " + appName);
        }
        log.info("创建执行器组: appName={}, groupId={}", appName, created.getInt("id"));
        return created.getInt("id");
    }

    /**
     * 按 AppName 查询执行器组。
     *
     * @param appName 执行器 AppName
     * @return 组信息，不存在返回 null
     */
    private JSONObject findGroup(String appName) {
        String body = HttpRequest.get(adminUrl + PATH_GROUP_PAGE)
                                 .timeout(REQUEST_TIMEOUT)
                                 .header("Cookie", cookie)
                                 .form("offset", 0)
                                 .form("pagesize", 100)
                                 .form("appname", appName)
                                 .form("title", "")
                                 .execute()
                                 .body();
        JSONArray data = contentData(body);
        for (Object item : data) {
            JSONObject group = (JSONObject) item;
            if (appName.equals(group.getStr("appname"))) {
                return group;
            }
        }
        return null;
    }

    /**
     * 按 handler 查询执行器组下的任务。
     *
     * @param groupId 执行器组 ID
     * @param handler 任务 Handler 名
     * @return 任务信息，不存在返回 null
     */
    private JSONObject findJob(int groupId, String handler) {
        String body = HttpRequest.get(adminUrl + PATH_JOB_PAGE)
                                 .timeout(REQUEST_TIMEOUT)
                                 .header("Cookie", cookie)
                                 .form("offset", 0)
                                 .form("pagesize", 20)
                                 .form("jobGroup", groupId)
                                 .form("triggerStatus", -1)
                                 .form("jobDesc", "")
                                 .form("executorHandler", handler)
                                 .form("author", "")
                                 .execute()
                                 .body();
        JSONArray data = contentData(body);
        for (Object item : data) {
            JSONObject job = (JSONObject) item;
            if (handler.equals(job.getStr("executorHandler"))) {
                return job;
            }
        }
        return null;
    }

    /**
     * 新增任务（BEAN 模式）。
     *
     * @param groupId 执行器组 ID
     * @param def 任务定义
     */
    private void addJob(int groupId, XxlJobProperties.JobDef def) {
        String body = HttpRequest.post(adminUrl + PATH_JOB_INSERT)
                                 .timeout(REQUEST_TIMEOUT)
                                 .header("Cookie", cookie)
                                 .form("jobGroup", groupId)
                                 .form("jobDesc", def.getDesc())
                                 .form("author", def.getAuthor())
                                 .form("alarmEmail", def.getAlarmEmail())
                                 .form("scheduleType", def.getScheduleType())
                                 .form("scheduleConf", def.getScheduleConf())
                                 .form("misfireStrategy", def.getMisfireStrategy())
                                 .form("executorRouteStrategy", def.getExecutorRouteStrategy())
                                 .form("executorHandler", def.getHandler())
                                 .form("executorParam", def.getExecutorParam())
                                 .form("executorBlockStrategy", def.getExecutorBlockStrategy())
                                 .form("executorTimeout", def.getExecutorTimeout())
                                 .form("executorFailRetryCount", def.getExecutorFailRetryCount())
                                 .form("glueType", "BEAN")
                                 .form("glueSource", "")
                                 .form("glueRemark", "")
                                 .form("childJobId", "")
                                 .execute()
                                 .body();
        checkResponse(body);
    }

    /**
     * 启动任务。
     *
     * @param jobId 任务 ID
     */
    private void startJob(int jobId) {
        String body = HttpRequest.post(adminUrl + PATH_JOB_START)
                                 .timeout(REQUEST_TIMEOUT)
                                 .header("Cookie", cookie)
                                 .form("ids[]", jobId)
                                 .execute()
                                 .body();
        checkResponse(body);
    }

    /**
     * 解析分页响应 content.data 数组。
     *
     * @param body 响应体
     * @return data 数组
     */
    private JSONArray contentData(String body) {
        JSONObject json = checkResponse(body);
        JSONObject content = json.getJSONObject("content");
        if (content == null) {
            return new JSONArray();
        }
        JSONArray data = content.getJSONArray("data");
        return data == null ? new JSONArray() : data;
    }

    /**
     * 校验调度中心响应码，失败抛出异常。
     *
     * @param body 响应体
     * @return 响应 JSON
     */
    private JSONObject checkResponse(String body) {
        JSONObject json = JSONUtil.parseObj(body);
        if (json.getInt("code", -1) != SUCCESS_CODE) {
            throw new IllegalStateException("调度中心返回失败: " + json.getStr("msg"));
        }
        return json;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * 供测试/排障使用的登录态。
     *
     * @return 会话 Cookie
     */
    public String getCookie() {
        return cookie;
    }
}
