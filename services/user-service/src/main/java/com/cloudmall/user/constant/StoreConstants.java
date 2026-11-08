package com.cloudmall.user.constant;

/**
 * 商家入驻常量。
 *
 * @author : Tomatos
 */
public interface StoreConstants {

    /** 商家角色 ID（cm_role.id=2，见种子数据） */
    long MERCHANT_ROLE_ID = 2L;

    /** 入驻通过后默认店铺描述 */
    String DEFAULT_SHOP_DESCRIPTION = "商家入驻店铺";

    /** 自动审核通过备注 */
    String AUTO_APPROVE_REMARK = "系统自动审核通过";

    /** 单次自动审核扫描条数 */
    int AUTO_REVIEW_BATCH_SIZE = 20;
}
