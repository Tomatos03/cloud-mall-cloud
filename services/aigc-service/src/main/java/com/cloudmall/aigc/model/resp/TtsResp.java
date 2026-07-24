package com.cloudmall.aigc.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本转语音响应
 * <p>
 * 当请求返回 JSON 格式时（非直接音频流），通过 base64Audio 字段返回音频数据。
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtsResp {

    /** Base64 编码的音频数据 */
    private String base64Audio;

    /** 音频格式（mp3 / opus / aac / flac / wav / pcm） */
    private String format;

    /** 音频字节数 */
    private Long size;
}
