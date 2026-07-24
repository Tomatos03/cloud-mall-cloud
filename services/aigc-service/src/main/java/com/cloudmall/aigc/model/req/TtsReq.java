package com.cloudmall.aigc.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.aigc.model.enums.TtsAudioFormat;
import com.cloudmall.aigc.model.enums.TtsVoice;

/**
 * 文本转语音请求参数
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtsReq {

    /** 待合成语音的文本 */
    @NotBlank(message = "text 不能为空")
    @Size(max = 4096, message = "text 长度不能超过 4096 个字符")
    private String text;

    /** 语音风格，详见 {@link TtsVoice} */
    private TtsVoice voice;

    /** 语速：0.25 ~ 4.0，默认 1.0 */
    private Double speed;

    /** 输出格式，详见 {@link TtsAudioFormat}，默认 MP3 */
    private TtsAudioFormat responseFormat;

    /** TTS 模型：tts-1, tts-1-hd, gpt-4o-mini-tts */
    private String model;
}
