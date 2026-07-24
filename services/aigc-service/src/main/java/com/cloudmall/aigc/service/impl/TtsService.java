package com.cloudmall.aigc.service.impl;

import java.util.Base64;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest.AudioResponseFormat;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import com.cloudmall.aigc.model.enums.TtsAudioFormat;
import com.cloudmall.aigc.model.enums.TtsVoice;
import com.cloudmall.aigc.model.req.TtsReq;
import com.cloudmall.aigc.model.resp.TtsResp;
import com.cloudmall.aigc.service.ITtsService;

/**
 * 文本转语音服务实现
 * <p>
 * 基于 Spring AI 的 TextToSpeechModel 调用 OpenAI TTS API 实现语音合成。
 * 支持多语音风格、语速调节、多种音频格式输出。
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Service
@RequiredArgsConstructor
public class TtsService implements ITtsService {

    private final TextToSpeechModel textToSpeechModel;

    /**
     * 文本转语音 — 同步调用，返回原始音频字节数组
     * <p>
     * 根据请求参数构建 TTS Prompt，调用 OpenAI TTS API 合成音频，
     * 等待完整响应后一次性返回全部音频数据。
     *
     * @param req 合成请求（含文本、语音风格、语速等参数）
     * @return 完整的音频字节数组
     */
    @Override
    public byte[] synthesize(TtsReq req) {
        TextToSpeechPrompt prompt = buildPrompt(req);
        return textToSpeechModel.call(prompt)
                                .getResult()
                                .getOutput();
    }

    /**
     * 文本转语音 — 同步调用，返回 Base64 编码的音频数据和元数据
     * <p>
     * 内部委托 {@link #synthesize(TtsReq)} 获取原始音频字节，
     * 再编码为 Base64 字符串，适用于 JSON 交互场景。
     *
     * @param req 合成请求
     * @return 包含 Base64 音频、格式和大小的响应对象
     */
    @Override
    public TtsResp synthesizeAsBase64(TtsReq req) {
        byte[] audioBytes = synthesize(req);
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
        String format = resolveFormat(req.getResponseFormat()).getCode();

        return TtsResp.builder()
                      .base64Audio(base64Audio)
                      .format(format)
                      .size((long) audioBytes.length)
                      .build();
    }

    /**
     * 文本转语音 — 流式调用，返回音频字节流
     * <p>
     * 实时将 TTS 模型的流式响应逐 chunk 映射为字节数组，
     * 客户端可在数据到达时立即开始播放，降低首包延迟。
     *
     * @param req 合成请求
     * @return 音频字节的响应式流
     */
    @Override
    public Flux<byte[]> stream(TtsReq req) {
        TextToSpeechPrompt prompt = buildPrompt(req);
        return textToSpeechModel.stream(prompt)
                                .map(response -> response.getResult().getOutput());
    }

    /**
     * 根据请求参数构建 TextToSpeechPrompt
     *
     * @param req 合成请求
     * @return TTS prompt
     */
    private TextToSpeechPrompt buildPrompt(TtsReq req) {
        OpenAiAudioSpeechOptions.Builder optionsBuilder = OpenAiAudioSpeechOptions.builder();

        if (req.getVoice() != null) {
            optionsBuilder.voice(req.getVoice().getCode());
        }
        if (req.getSpeed() != null) {
            optionsBuilder.speed(req.getSpeed());
        }
        if (req.getResponseFormat() != null) {
            optionsBuilder.responseFormat(toAudioResponseFormat(req.getResponseFormat()));
        }
        if (req.getModel() != null) {
            optionsBuilder.model(req.getModel());
        }

        OpenAiAudioSpeechOptions options = optionsBuilder.build();
        return new TextToSpeechPrompt(req.getText(), options);
    }

    /**
     * 将枚举类型的音频格式转换为 Spring AI 的 AudioResponseFormat
     *
     * @param format 音频格式枚举
     * @return Spring AI 音频格式
     */
    private AudioResponseFormat toAudioResponseFormat(TtsAudioFormat format) {
        return switch (format) {
            case OPUS -> AudioResponseFormat.OPUS;
            case AAC -> AudioResponseFormat.AAC;
            case FLAC -> AudioResponseFormat.FLAC;
            case WAV -> AudioResponseFormat.WAV;
            case PCM -> AudioResponseFormat.PCM;
            default -> AudioResponseFormat.MP3;
        };
    }

    /**
     * 解析输出格式，为空时返回默认 MP3
     *
     * @param responseFormat 请求中的格式枚举
     * @return 格式枚举（默认 MP3）
     */
    private TtsAudioFormat resolveFormat(TtsAudioFormat responseFormat) {
        return responseFormat != null ? responseFormat : TtsAudioFormat.MP3;
    }
}
