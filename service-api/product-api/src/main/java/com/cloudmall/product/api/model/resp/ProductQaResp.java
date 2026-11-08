package com.cloudmall.product.api.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品问答响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductQaResp {

    /** 问答 ID */
    private Long id;

    /** 商品 SPU ID */
    private Long productId;

    /** 问题内容 */
    private String question;

    /** 回答内容，未回答时为 null */
    private String answer;

    /** 有用数 */
    private Integer helpfulCount;

    /** 创建时间 */
    private LocalDateTime createTime;
}
