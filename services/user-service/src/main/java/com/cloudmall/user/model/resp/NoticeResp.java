package com.cloudmall.user.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页公告响应
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResp {

    /** 公告 ID */
    private Long id;

    /** 公告内容 */
    private String content;
}
