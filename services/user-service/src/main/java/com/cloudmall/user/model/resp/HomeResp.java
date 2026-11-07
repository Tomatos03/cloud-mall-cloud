package com.cloudmall.user.model.resp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.product.api.model.resp.BannerResp;
import com.cloudmall.product.api.model.resp.CategoryResp;
import com.cloudmall.seckill.api.model.resp.ActivityDetailResp;

/**
 * 首页聚合响应
 * <p>
 * 聚合公告、轮播图、分类、当前整点秒杀活动与分类展示商品，前端首页一次请求获取全部数据。
 * 各信息块获取失败时降级为空列表/null，不影响其余块。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeResp {

    /** 首页公告 */
    private List<NoticeResp> notices;

    /** 商品轮播图（最多 5 个） */
    private List<BannerResp> banners;

    /** 商品分类信息（平铺，含层级关系） */
    private List<CategoryResp> categories;

    /** 当前整点时刻秒杀活动（含已审核商品），无进行中场次时为 null */
    private ActivityDetailResp seckillActivity;

    /** 分类展示商品（按一级分类分组） */
    private List<HomeCategoryProductResp> categoryProduct;
}
