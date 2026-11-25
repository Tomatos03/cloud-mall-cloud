package com.cloudmall.im.infrastructure.support.impl;

import java.util.UUID;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cloudmall.im.infrastructure.support.IInstanceIdProvider;

/**
 * 默认实例 ID 提供者。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
@Component
public class InstanceIdProvider implements IInstanceIdProvider {

    private final String instanceId;

    public InstanceIdProvider(@Value("${cloudmall.im.instance-id:}") String configured) {
        this.instanceId = StrUtil.isBlank(configured) ? UUID.randomUUID().toString() : configured;
    }

    @Override
    public String get() {
        return instanceId;
    }
}
