package com.cloudmall.aigc.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 卡片中的键值字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCardFieldResp {

    /** 字段名 */
    private String label;

    /** 字段值 */
    private String value;
}
