package com.cloudmall.product.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品图片。
 * <p>
 * 代表商品的一张图片。主图与排序均由服务端按数组下标推导：
 * 列表首张为主图（{@code cm_product.image_id}），其余按下标作为详情页轮播顺序，
 * 客户端无需（也无法）传入 isMain 与 sortOrder。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageItem {

    @NotBlank(message = "图片 URL 不能为空")
    private String url;
}
