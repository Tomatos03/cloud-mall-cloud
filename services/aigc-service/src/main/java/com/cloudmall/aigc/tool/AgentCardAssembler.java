package com.cloudmall.aigc.tool;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import com.cloudmall.aigc.model.resp.AgentCardFieldResp;
import com.cloudmall.aigc.model.resp.AgentCardResp;
import com.cloudmall.coupon.api.enums.CouponEnum;
import com.cloudmall.coupon.api.model.resp.CouponResp;
import com.cloudmall.coupon.api.model.resp.UserCouponResp;
import com.cloudmall.order.api.model.resp.OrderItemResp;
import com.cloudmall.order.api.model.resp.OrderResp;
import com.cloudmall.product.api.model.resp.ProductResp;
import com.cloudmall.search.api.model.resp.SearchProductResp;

/**
 * 将工具查询到的业务对象转换为前端可渲染的卡片。
 */
@Component
public class AgentCardAssembler {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Map<String, String> orderStatusText = Map.of(
            "CREATING",
            "创建中",
            "PENDING_PAYMENT",
            "待支付",
            "PAID",
            "已支付",
            "TIMEOUT",
            "超时关闭",
            "CANCELLED",
            "已取消",
            "PARTIAL_REFUNDED",
            "部分退款",
            "REFUNDED",
            "已全额退款"
    );
    private final Map<String, String> couponStatusText = Map.of(
            "AVAILABLE",
            "可使用",
            "LOCKED",
            "已锁定",
            "USED",
            "已使用",
            "EXPIRED",
            "已过期",
            "REVOKED",
            "已回收"
    );
    private final Map<String, String> couponOwnerText = Map.of("PLATFORM", "平台券", "SHOP", "店铺券");
    private final Map<String, String> couponScopeText = Map.of(
            "ALL",
            "全范围",
            "PRODUCT",
            "指定商品",
            "CATEGORY",
            "指定分类"
    );

    /**
     * 商品详情卡片。
     *
     * @param product 商品详情
     * @return 商品卡片
     */
    public AgentCardResp product(ProductResp product) {
        if (product == null) {
            return null;
        }
        List<AgentCardFieldResp> fields = new ArrayList<>();
        addField(fields, "SPU", product.getSpuCode());
        return AgentCardResp.builder()
                            .type("product")
                            .id(stringId(product.getId()))
                            .title(StrUtil.blankToDefault(product.getName(), "商品详情"))
                            .subtitle(joinNonBlank(product.getBrand(), product.getCategoryName()))
                            .image(product.getImage())
                            .badge(money(product.getPrice()))
                            .fields(fields)
                            .actionText("查看商品")
                            .actionUrl(product.getId() == null ? null : "/product/" + product.getId())
                            .build();
    }

    /**
     * 商品搜索结果卡片。
     *
     * @param products 商品搜索结果
     * @return 商品卡片列表
     */
    public List<AgentCardResp> products(List<SearchProductResp> products) {
        if (CollUtil.isEmpty(products)) {
            return List.of();
        }
        return products.stream()
                       .filter(Objects::nonNull)
                       .map(this::product)
                       .toList();
    }

    /**
     * 订单卡片。
     *
     * @param order 订单详情
     * @return 订单卡片
     */
    public AgentCardResp order(OrderResp order) {
        if (order == null) {
            return null;
        }
        List<AgentCardFieldResp> fields = new ArrayList<>();
        addField(fields, "应付", money(order.getPayableAmount()));
        int itemCount = itemCount(order);
        if (itemCount > 0) {
            addField(fields, "商品", itemCount + " 件");
        }
        addField(fields, "下单时间", formatDateTime(order.getCreateTime()));
        return AgentCardResp
                .builder()
                .type("order")
                .id(order.getOrderNo())
                .title(StrUtil.blankToDefault(firstItemName(order), "订单详情"))
                .subtitle(StrUtil.isBlank(order.getOrderNo()) ? null : "订单号 " + order.getOrderNo())
                .badge(mapValue(orderStatusText, order.getStatus()))
                .fields(fields)
                .actionText("查看订单")
                .actionUrl("/profile?tab=orders")
                .build();
    }

    /**
     * 订单列表卡片。
     *
     * @param orders 订单列表
     * @return 订单卡片列表
     */
    public List<AgentCardResp> orders(List<OrderResp> orders) {
        if (CollUtil.isEmpty(orders)) {
            return List.of();
        }
        return orders.stream()
                     .filter(Objects::nonNull)
                     .map(this::order)
                     .toList();
    }

    /**
     * 可领取优惠券卡片。
     *
     * @param coupon 优惠券模板
     * @return 优惠券卡片
     */
    public AgentCardResp coupon(CouponResp coupon) {
        if (coupon == null) {
            return null;
        }
        List<AgentCardFieldResp> fields = new ArrayList<>();
        addField(fields, "门槛", couponThreshold(coupon.getThreshold()));
        addField(fields, "有效期", couponPeriod(coupon));

        String statusText = Boolean.TRUE.equals(coupon.getClaimable())
                ? "可领取"
                : Boolean.TRUE.equals(coupon.getClaimed()) ? "已领取" : "暂不可领";
        return AgentCardResp
                .builder()
                .type("coupon")
                .id(stringId(coupon.getId()))
                .title(StrUtil.blankToDefault(coupon.getName(), "优惠券"))
                .subtitle(
                        joinNonBlank(
                                mapValue(couponOwnerText, coupon.getOwnerType()),
                                mapValue(couponScopeText, coupon.getScopeType()),
                                statusText
                        )
                )
                .badge(couponValue(coupon.getType(), coupon.getDiscountAmount(), coupon.getDiscountRate()))
                .fields(fields)
                .actionText("去优惠券中心")
                .actionUrl("/profile?tab=coupons")
                .build();
    }

    /**
     * 可领取优惠券列表卡片。
     *
     * @param coupons 优惠券模板列表
     * @return 优惠券卡片列表
     */
    public List<AgentCardResp> coupons(List<CouponResp> coupons) {
        if (CollUtil.isEmpty(coupons)) {
            return List.of();
        }
        return coupons.stream()
                      .filter(Objects::nonNull)
                      .map(this::coupon)
                      .toList();
    }

    /**
     * 用户优惠券卡片。
     *
     * @param coupon 用户券
     * @return 优惠券卡片
     */
    public AgentCardResp userCoupon(UserCouponResp coupon) {
        if (coupon == null) {
            return null;
        }
        List<AgentCardFieldResp> fields = new ArrayList<>();
        addField(fields, "门槛", couponThreshold(coupon.getThresholdSnapshot()));
        addField(fields, "有效期", userCouponPeriod(coupon));
        String status = coupon.getStatus();
        return AgentCardResp
                .builder()
                .type("coupon")
                .id(stringId(coupon.getId()))
                .title(StrUtil.blankToDefault(coupon.getNameSnapshot(), "优惠券"))
                .subtitle(
                        joinNonBlank(
                                mapValue(couponOwnerText, coupon.getOwnerType()),
                                mapValue(couponStatusText, status)
                        )
                )
                .badge(
                        couponValue(
                                coupon.getTypeSnapshot(),
                                coupon.getDiscountAmountSnapshot(),
                                coupon.getDiscountRateSnapshot()
                        )
                )
                .fields(fields)
                .actionText("AVAILABLE".equals(status) ? "去使用" : "查看优惠券")
                .actionUrl("/profile?tab=coupons")
                .build();
    }

    /**
     * 用户优惠券列表卡片。
     *
     * @param coupons 用户券列表
     * @return 优惠券卡片列表
     */
    public List<AgentCardResp> userCoupons(List<UserCouponResp> coupons) {
        if (CollUtil.isEmpty(coupons)) {
            return List.of();
        }
        return coupons.stream()
                      .filter(Objects::nonNull)
                      .map(this::userCoupon)
                      .toList();
    }

    private AgentCardResp product(SearchProductResp product) {
        List<AgentCardFieldResp> fields = new ArrayList<>();
        if (product.getSales() != null) {
            addField(fields, "销量", String.valueOf(product.getSales()));
        }
        return AgentCardResp.builder()
                            .type("product")
                            .id(stringId(product.getId()))
                            .title(StrUtil.blankToDefault(product.getName(), "商品"))
                            .subtitle(joinNonBlank(product.getBrandName(), product.getCategoryName()))
                            .image(product.getMainImage())
                            .badge(money(product.getPriceMin()))
                            .fields(fields)
                            .actionText("查看商品")
                            .actionUrl(product.getId() == null ? null : "/product/" + product.getId())
                            .build();
    }

    private String firstItemName(OrderResp order) {
        if (CollUtil.isEmpty(order.getSubs())) {
            return null;
        }
        return order.getSubs()
                    .stream()
                    .filter(Objects::nonNull)
                    .flatMap(sub -> sub.getItems() == null ? Stream.empty() : sub.getItems().stream())
                    .map(OrderItemResp::getProductName)
                    .filter(StrUtil::isNotBlank)
                    .findFirst()
                    .orElse(null);
    }

    private int itemCount(OrderResp order) {
        if (CollUtil.isEmpty(order.getSubs())) {
            return 0;
        }
        return order.getSubs()
                    .stream()
                    .filter(Objects::nonNull)
                    .mapToInt(sub -> sub.getItems() == null ? 0 : sub.getItems().size())
                    .sum();
    }

    private String couponValue(String type, BigDecimal discountAmount, BigDecimal discountRate) {
        if (CouponEnum.DISCOUNT.getCode().equals(type) && discountRate != null) {
            BigDecimal discount = BigDecimal.ONE.subtract(discountRate)
                                                .multiply(BigDecimal.TEN)
                                                .stripTrailingZeros();
            return discount.toPlainString() + "折";
        }
        return money(discountAmount);
    }

    private String couponThreshold(BigDecimal threshold) {
        if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0) {
            return "无门槛";
        }
        return "满 " + money(threshold) + " 可用";
    }

    private String couponPeriod(CouponResp coupon) {
        if (coupon.getFixedStartTime() != null && coupon.getFixedExpireTime() != null) {
            return formatDate(coupon.getFixedStartTime()) + " ~ " + formatDate(coupon.getFixedExpireTime());
        }
        if (coupon.getValidDays() != null) {
            return "领取后 " + coupon.getValidDays() + " 天";
        }
        if (coupon.getIssueEndTime() != null) {
            return "发放至 " + formatDate(coupon.getIssueEndTime());
        }
        return null;
    }

    private String userCouponPeriod(UserCouponResp coupon) {
        if (coupon.getEffectiveTime() == null || coupon.getExpireTime() == null) {
            return null;
        }
        return formatDate(coupon.getEffectiveTime()) + " ~ " + formatDate(coupon.getExpireTime());
    }

    private String formatDateTime(LocalDateTime time) {
        return time == null ? null : dateTimeFormatter.format(time);
    }

    private String formatDate(LocalDateTime time) {
        return time == null ? null : dateFormatter.format(time);
    }

    private String money(BigDecimal amount) {
        return amount == null ? null : "¥" + amount.stripTrailingZeros().toPlainString();
    }

    private String money(Double amount) {
        return amount == null ? null : money(BigDecimal.valueOf(amount));
    }

    private String stringId(Long id) {
        return id == null ? null : String.valueOf(id);
    }

    private String joinNonBlank(String... values) {
        String joined = Stream.of(values)
                              .filter(StrUtil::isNotBlank)
                              .collect(Collectors.joining(" · "));
        return StrUtil.emptyToNull(joined);
    }

    private String mapValue(Map<String, String> map, String key) {
        return StrUtil.isBlank(key) ? null : map.getOrDefault(key, key);
    }

    private void addField(List<AgentCardFieldResp> fields, String label, String value) {
        if (StrUtil.isBlank(value)) {
            return;
        }
        fields.add(AgentCardFieldResp.builder()
                                     .label(label)
                                     .value(value)
                                     .build());
    }
}
