package com.cloudmall.user.security;

import java.util.regex.Pattern;

/**
 * 身份证号格式与校验位校验。
 *
 * @author : Tomatos
 */
public final class IdCardValidator {

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private IdCardValidator() {}

    /**
     * 校验身份证号。
     *
     * @param idCardNo 身份证号
     * @return 是否合法
     */
    public static boolean isValid(String idCardNo) {
        if (idCardNo == null || !ID_CARD_PATTERN.matcher(idCardNo).matches()) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Character.digit(idCardNo.charAt(i), 10) * WEIGHTS[i];
        }
        char expected = CHECK_CODES[sum % 11];
        return Character.toUpperCase(idCardNo.charAt(17)) == expected;
    }
}
