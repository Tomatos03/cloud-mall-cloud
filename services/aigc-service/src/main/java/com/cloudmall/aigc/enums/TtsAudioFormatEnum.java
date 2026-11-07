package com.cloudmall.aigc.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * TTS 音频输出格式枚举
 * <p>
 * 对应 OpenAI TTS API 支持的音频输出格式。
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Getter
public enum TtsAudioFormatEnum {
    MP3("mp3", "MP3"),
    OPUS("opus", "Opus"),
    AAC("aac", "AAC"),
    FLAC("flac", "FLAC"),
    WAV("wav", "WAV"),
    PCM("pcm", "PCM");
    /** API 请求参数值 */
    @JsonValue
    private final String code;
    /** 格式名称 */
    private final String desc;
    TtsAudioFormatEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    /**
     * 根据 code 解析枚举
     *
     * @param code 格式 code（如 "mp3"）
     * @return 匹配的枚举
     * @throws IllegalArgumentException code 为 null 或无法匹配
     *                                  时抛出
     */
    public static TtsAudioFormatEnum fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("code must not be null");
        }
        return Arrays.stream(values())
                     .filter(format -> format.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown TtsAudioFormatEnum code: " + code));
    }
}
