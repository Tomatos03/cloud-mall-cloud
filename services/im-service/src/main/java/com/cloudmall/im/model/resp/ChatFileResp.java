package com.cloudmall.im.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.im.enums.MessageTypeEnum;

/**
 * 聊天文件上传结果
 *
 * @author : Tomatos
 * @date : 2026/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFileResp {

    /** 文件访问路径（相对 nginx 源站） */
    private String url;

    /** 文件对应的消息类型（image / video） */
    private MessageTypeEnum type;

    /** 原始文件名 */
    private String name;

    /** 文件大小（字节） */
    private Long size;
}
