package com.cloudmall.coupon.cache;

import java.time.Duration;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.cloudmall.coupon.api.model.resp.CouponResp;

/**
 * 公开券中心缓存服务
 * <p>
 * 缓存匿名视角的公开模板列表，领取或模板操作后失效。
 * 用户视角的 claimed/claimable 不入缓存，由 Service 层实时组装。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class CouponCacheService {

    /** 公开券中心缓存键 */
    private static final String PUBLIC_TEMPLATES_KEY = "coupon:public:templates";

    /** 缓存时长 */
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 缓存公开模板列表（匿名视角）。
     *
     * @param resps 模板响应列表
     */
    public void putPublicTemplates(List<CouponResp> resps) {
        if (CollUtil.isEmpty(resps)) {
            return;
        }
        stringRedisTemplate.opsForValue().set(PUBLIC_TEMPLATES_KEY, JSONUtil.toJsonStr(resps), CACHE_TTL);
    }

    /**
     * 读取公开模板列表缓存。
     *
     * @return 模板响应列表，无缓存返回 null
     */
    public List<CouponResp> getPublicTemplates() {
        String json = stringRedisTemplate.opsForValue().get(PUBLIC_TEMPLATES_KEY);
        if (json == null) {
            return null;
        }
        return JSONUtil.toList(json, CouponResp.class);
    }

    /**
     * 失效公开券中心缓存（领取或模板操作后调用）。
     */
    public void evictPublicTemplates() {
        stringRedisTemplate.delete(PUBLIC_TEMPLATES_KEY);
    }
}
