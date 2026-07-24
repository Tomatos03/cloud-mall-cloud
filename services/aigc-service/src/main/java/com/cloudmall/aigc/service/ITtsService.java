package com.cloudmall.aigc.service;

import reactor.core.publisher.Flux;

import com.cloudmall.aigc.model.req.TtsReq;
import com.cloudmall.aigc.model.resp.TtsResp;

/**
 * 文本转语音服务接口
 * <p>
 * 将文本合成为自然语音，支持多种语音风格、语速和输出格式。
 * 底层使用 OpenAI TTS API（tts-1 / tts-1-hd / gpt-4o-mini-tts）。
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
public interface ITtsService {

    /**
     * 文本转语音 — 返回音频字节数组
     * <p>
     * 直接返回原始音频数据，适用于需要直接播放的场景。
     *
     * @param req 合成请求（含文本、语音风格、语速等参数）
     * @return 音频字节数组
     */
    byte[] synthesize(TtsReq req);

    /**
     * 文本转语音 — 返回 Base64 编码的音频数据
     * <p>
     * 适用于 JSON 交互场景，客户端无法直接处理二进制流时使用。
     *
     * @param req 合成请求（含文本、语音风格、语速等参数）
     * @return 包含 Base64 音频和元数据的响应
     */
    TtsResp synthesizeAsBase64(TtsReq req);

    /**
     * 流式文本转语音 — 返回音频字节流
     * <p>
     * 实时边合成边输出，客户端可在音频数据到达时立即开始播放。
     * 适合长文本场景，减少首包延迟。
     *
     * @param req 合成请求
     * @return 音频字节的响应流
     */
    Flux<byte[]> stream(TtsReq req);
}
