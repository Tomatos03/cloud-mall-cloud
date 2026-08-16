package com.cloudmall.aigc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import com.cloudmall.aigc.model.req.TtsReq;
import com.cloudmall.aigc.model.resp.TtsResp;
import com.cloudmall.aigc.service.ITtsService;
import com.cloudmall.common.model.Result;

/**
 * 文本转语音接口
 * <p>
 * 将文本合成为自然语音，支持多语音风格、语速调节和多种音频格式输出。
 * 提供三种调用方式：
 * <ul>
 *   <li>直接返回音频字节流（适合直接播放）</li>
 *   <li>Base64 编码返回（适合 JSON 交互）</li>
 *   <li>流式音频返回（边合成边播放，降低首包延迟）</li>
 * </ul>
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@RestController
@RequestMapping("/tts")
@RequiredArgsConstructor
public class TtsController {

    private final ITtsService ttsService;

    /**
     * 文本转语音 — 直接返回 MP3 音频字节 <p>
     * 客户端可直接播放返回的音频数据，适合浏览器 Audio API 或 移动端原生播放器使用。
     *
     * @param req 合成请求（含文本、语音风格等参数）
     * @return MP3 音频字节数组
     */
    @PostMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] synthesize(@Valid @RequestBody TtsReq req) {
        return ttsService.synthesize(req);
    }

    /**
     * 文本转语音 — 返回 Base64 编码的音频数据和元数据 <p>
     * 适用于 JSON 交互场景，客户端无法直接处理二进制流时使用。
     *
     * @param req 合成请求
     * @return 包含 Base64 音频、格式和大小的响应
     */
    @PostMapping("/base64")
    public Result<TtsResp> synthesizeAsBase64(@Valid @RequestBody TtsReq req) {
        return Result.success(ttsService.synthesizeAsBase64(req));
    }

    /**
     * 流式文本转语音 — SSE 方式逐 chunk 返回音频字节 <p>
     * 服务端边合成边推送音频字节流，客户端可在数据到达时立即开始播放， 显著降低长文本的首包延迟。
     *
     * @param req 合成请求
     * @return SSE 流，每个 chunk 包含一段音频字节
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<byte[]> stream(@Valid @RequestBody TtsReq req) {
        return ttsService.stream(req);
    }
}
