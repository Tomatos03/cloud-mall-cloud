package com.cloudmall.aigc.model.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * TTS 语音风格枚举
 * <p>
 * 对应 OpenAI TTS API 支持的语音选项。
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Getter
public enum TtsVoice {
    ALLOY("alloy", "Alloy（中性）"),
    ECHO("echo", "Echo（沉稳男声）"),
    FABLE("fable", "Fable（叙事）"),
    NOVA("nova", "Nova（温暖女声）"),
    SHIMMER("shimmer", "Shimmer（清亮）"),
    ONYX("onyx", "Onyx（低沉）"),
    SAGE("sage", "Sage（睿智）"),
    CORAL("coral", "Coral（活力）"),
    ASH("ash", "Ash（柔和）"),
    BALLAD("ballad", "Ballad（抒情）"),
    VERSE("verse", "Verse（诗意）");
    /** API 请求参数值 */
    @JsonValue
    private final String code;
    /** 中文描述 */
    private final String desc;
    TtsVoice(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    /**
     * 根据 code 解析枚举
     *
     * @param code 语音风格的 code（如 "alloy"）
     * @return 匹配的枚举
     * @throws IllegalArgumentException code 为 null 或无法匹配
     *                                  时抛出
     */
    public static TtsVoice fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("code must not be null");
        }
        return Arrays.stream(values())
                     .filter(voice -> voice.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown TtsVoice code: " + code));
    }
}
