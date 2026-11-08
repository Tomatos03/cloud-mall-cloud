package com.cloudmall.user.api.model.resp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户商品浏览记录。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowseHistoryResp {

    private Long id;

    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal price;

    private LocalDateTime browseTime;
}
