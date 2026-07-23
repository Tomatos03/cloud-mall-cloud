package com.cloudmall.aigc.model.enums;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 会话时间分类枚举
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@Getter
public enum SessionTimeBucket {

    TODAY("当天") {
        @Override
        public boolean matches(LocalDateTime updateTime, LocalDateTime now) {
            return LocalDateTimeUtil.isSameDay(updateTime, now);
        }
    },
    LAST_30_DAYS("最近30天") {
        @Override
        public boolean matches(LocalDateTime updateTime, LocalDateTime now) {
            return !LocalDateTimeUtil.isSameDay(updateTime, now)
                    && LocalDateTimeUtil.between(updateTime, now, ChronoUnit.DAYS) < 30;
        }
    },
    LAST_1_YEAR("最近1年") {
        @Override
        public boolean matches(LocalDateTime updateTime, LocalDateTime now) {
            long days = LocalDateTimeUtil.between(updateTime, now, ChronoUnit.DAYS);
            return days >= 30
                    && LocalDateTimeUtil.between(updateTime, now, ChronoUnit.YEARS) < 1;
        }
    },
    OLDER("1年以上") {
        @Override
        public boolean matches(LocalDateTime updateTime, LocalDateTime now) {
            return LocalDateTimeUtil.between(updateTime, now, ChronoUnit.YEARS) >= 1;
        }
    };

    /**
     * -- GETTER --
     *  获取分类显示名称
     *
     * @return 中文显示名
     */
    private final String displayName;

    SessionTimeBucket(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 判断指定时间是否属于当前分类
     *
     * @param updateTime 会话更新时间
     * @param now        当前时间
     * @return true 如果属于当前分类
     */
    public abstract boolean matches(LocalDateTime updateTime, LocalDateTime now);

}
