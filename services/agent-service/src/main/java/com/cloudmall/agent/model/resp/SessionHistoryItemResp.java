package com.cloudmall.agent.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 历史会话列表条目
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionHistoryItemResp {

    private String sessionId;

    private String title;

    private String updateTime;
}
