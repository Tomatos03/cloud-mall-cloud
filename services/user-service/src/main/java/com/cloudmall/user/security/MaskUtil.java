package com.cloudmall.user.security;

import cn.hutool.core.util.StrUtil;

/**
 * 敏感信息脱敏工具。
 *
 * @author : Tomatos
 */
public final class MaskUtil {

    private MaskUtil() {}

    /**
     * 姓名脱敏：保留首字符，其余替换为星号。
     *
     * @param realName 真实姓名
     * @return 脱敏姓名
     */
    public static String maskName(String realName) {
        if (StrUtil.isBlank(realName)) {
            return null;
        }
        return realName.charAt(0) + "*".repeat(Math.max(1, realName.length() - 1));
    }

    /**
     * 身份证号脱敏：保留前 3 位与后 4 位。
     *
     * @param idCardNo 身份证号
     * @return 脱敏身份证号
     */
    public static String maskIdCard(String idCardNo) {
        if (StrUtil.isBlank(idCardNo) || idCardNo.length() <= 7) {
            return "****";
        }
        return idCardNo.substring(0, 3) + "*".repeat(idCardNo.length() - 7)
                       + idCardNo.substring(idCardNo.length() - 4);
    }
}
