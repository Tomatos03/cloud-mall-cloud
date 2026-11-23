package com.cloudmall.shared.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.shared.enums.ChatMediaTypeEnum;

/**
 * 聊天文件上传结果。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFileResp {

    /** 文件台账 ID */
    private Long id;

    /** 文件访问路径（相对网关源站） */
    private String url;

    /** 文件对应的媒体类型（image / video） */
    private ChatMediaTypeEnum type;

    /** 原始文件名 */
    private String name;

    /** 文件大小（字节） */
    private Long size;
}
