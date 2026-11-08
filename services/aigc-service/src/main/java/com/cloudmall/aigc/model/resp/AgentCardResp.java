package com.cloudmall.aigc.model.resp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话中的结构化卡片，随 PARAM 事件下发给前端渲染。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCardResp {

    /** 卡片类型：product / order / coupon */
    private String type;

    /** 业务标识：商品 ID、订单号或用户券 ID */
    private String id;

    /** 卡片主标题 */
    private String title;

    /** 卡片副标题 */
    private String subtitle;

    /** 商品图片等媒体地址 */
    private String image;

    /** 右上角强调信息，如价格或订单状态 */
    private String badge;

    /** 卡片字段列表 */
    private List<AgentCardFieldResp> fields;

    /** 跳转按钮文案 */
    private String actionText;

    /** 前端站内跳转地址 */
    private String actionUrl;
}
