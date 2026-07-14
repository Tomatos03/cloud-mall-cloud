package com.cloudmall.common.enums;

import lombok.Getter;

/**
 * 业务错误码枚举
 * 统一管理系统中所有业务异常的错误码和错误信息
 *
 * @author : Tomatos
 * @date : 2025/12/18
 */
@Getter
public enum BizErrorCode {
    
    // ==================== 通用错误码 (1xxx) ====================
    INVALID_PARAM(1000, "请求参数无效"),
    INVALID_AUDIT_STATUS(1001, "无效的审核状态"),
    DATA_NOT_FOUND(1002, "数据不存在"),

    // ==================== 用户认证相关错误码 (10xxx) ====================
    USERNAME_OR_PASSWORD_ERROR(10000, "用户名或密码错误"),
    UNKNOWN_ROLE(10001, "未知角色"),
    USER_ALREADY_EXISTS(10002, "用户已经存在"),
    USER_NOT_EXISTS(10003, "用户不存在"),
    PASSWORD_NOT_MATCH(10004, "密码不匹配"),
    PASSWORD_ERROR(10010, "密码错误"),
    USER_NOT_AUTHENTICATED(10005, "用户未认证"),
    MERCHANT_STORE_NOT_FOUND(10006, "商户店铺不存在"),
    NO_MANAGE_PERMISSION(10007, "无管理端访问权限"),
    NOT_MERCHANT(10008, "您还不是商家，请先申请入驻"),
    INVALID_CLIENT_TYPE(10009, "无效的客户端类型"),

    // ==================== 商品分类相关错误码 (20xxx) ====================
    CATEGORY_BEYOND_MAX_LEVEL(20000, "分类层级超出最大限制"),
    CATEGORY_NOT_EXIST_OR_NO_ENABLE(20013, "分类不存在"),
    GOODS_OR_SHOP_NOT_EXIST(20001, "商品或店铺不存在"),
    GOODS_SAVE_FAILED(20002, "商品保存失败"),
    SKU_SAVE_FAILED(20003, "SKU保存失败"),
    SPEC_VALUE_INVALID(20004, "规格值无效"),
    GOODS_ID_INVALID(20005, "商品ID无效"),
    GOODS_UPDATE_FAILED(20006, "商品更新失败"),
    SPEC_SAVE_FAILED(20007, "规格保存失败"),
    SPEC_VALUE_SAVE_FAILED(20008, "规格值保存失败"),
    SPU_DELETE_FAILED(20009, "删除商品SPU失败"),
    SPEC_VALUE_DELETE_FAILED(20010, "规格值删除失败"),
    SPEC_VALUE_CLEANUP_NOT_SUPPORTED(20011, "规格值清理功能不支持"),
    GOODS_PUBLISH_PAYLOAD_INVALID(20012, "商品发布数据无效"),
    SPECIFICATIONS_CANNOT_BE_EMPTY(20014, "规格不能为空"),
    SKUS_CANNOT_BE_EMPTY(20015, "SKU不能为空"),
    SPECIFICATIONS_EXCEED_MAX_LIMIT(20016, "规格数量超出最大限制"),
     SKU_SPECS_CANNOT_BE_EMPTY(20017, "SKU规格不能为空"),
     SKU_SPEC_NOT_MATCH(20018, "SKU中包含无效的规格"),
     GOODS_UNIT_NOT_EXIST(20019, "商品单位不存在"),

     // ==================== 地址相关错误码 (30xxx) ====================
    ADDRESS_NOT_EXIST(30000, "地址不存在"),

    // ==================== 搜索相关错误码 (40xxx) ====================
    UNKNOWN_SEARCH_ORDER_TYPE(40000, "未知的搜索排序类型"),

    // ==================== 轮播图相关错误码 (45xxx) ====================
    BANNER_NOT_EXIST(45000, "轮播图不存在"),
    BANNER_GOODS_NOT_EXIST(45001, "轮播图关联的商品不存在"),

    // ==================== 订单相关错误码 (50xxx) ====================
    // 订单创建相关
    CREATE_ORDER_FAILURE(50000, "创建订单失败"),
    ORDER_DATA_IS_NULL(50001, "订单数据为空"),
    ORDER_GOODS_ID_IS_NULL(50002, "商品ID为空"),
    ORDER_QUANTITY_INVALID(50003, "商品数量无效"),
    GOODS_NOT_IN_CART(50004, "购物车中不存在该商品"),
    GOODS_NOT_EXIST(50005, "商品不存在"),
    GOODS_OFF_SHELF(50006, "商品已下架"),
    GOODS_STOCK_INSUFFICIENT(50007, "商品库存不足"),
    ORDER_CREATE_FAILED(50012, "订单创建失败"),
    
    // 订单状态相关
    INVALID_ORDER_STATUS(50008, "无效的订单状态"),
    ORDER_NOT_EXIST(50009, "订单不存在"),
    ORDER_NOT_BELONG_TO_USER(50010, "订单不属于当前用户"),
    INVALID_ORDER_TYPE(50013, "无效的订单类型"),
    ORDER_STATUS_NOT_PAID(50014, "订单状态不是待发货，无法发货"),
    ORDER_STATUS_NOT_CREATED(50015, "订单状态不是待支付，无法取消"),
    ORDER_STATUS_NOT_FINISHED(50017, "订单未完成，无法进行此操作"),
    
    // 订单评价相关
    ORDER_NO_COMMENT(50016, "订单无买家评价，无法回复"),
    ORDER_ITEM_NOT_EXIST(50018, "订单明细不存在"),
    
    // 已废弃或很少使用
    USER_ID_IS_NULL(50011, "用户ID不能为空"),  // 拦截器保证非空，不再使用
    GOODS_INVENTORY_NOT_ENOUGH(50019, "SKU库存不足"),
    ORDER_CREATE_ADDRESS_NOT_EXIST(50020, "下单地址不存在"),
    ORDER_CREATE_ITEMS_EMPTY(50021, "下单商品项不能为空"),
    ORDER_CREATE_STORE_NOT_EXIST(50022, "下单店铺不存在"),
    ORDER_CREATE_SKU_NOT_AVAILABLE(50023, "下单SKU不存在或已下架"),
    ORDER_CREATE_SKU_STORE_MISMATCH(50024, "下单SKU不属于当前店铺"),
    ORDER_CREATE_SKU_INVENTORY_NOT_ENOUGH(50025, "下单SKU库存不足"),

    // ==================== 购物车相关错误码 (60xxx) ====================
    UNKNOW_CART_TYPE(60000, "未知的购物车类型"),
    CART_GOODS_NOT_EXIST(60001, "商品不存在"),
    CART_GOODS_NOT_BELONG_TO_STORE(60002, "商品不属于该店铺"),
    CART_GOODS_OFF_SHELF(60003, "商品已下架"),
    CART_GOODS_STOCK_INSUFFICIENT(60004, "商品库存不足"),
    CART_STORE_NOT_EXIST(60005, "店铺不存在"),
    CART_ITEM_NOT_EXIST(60006, "购物车中不存在该商品"),

    // ==================== 商品评论相关错误码 (70xxx) ====================
    // 评论参数验证相关
    COMMENT_RATING_INVALID(70000, "评分必须在1-5之间"),
    COMMENT_CONTENT_EMPTY(70001, "评论内容不能为空"),
    COMMENT_GOODS_NAME_EMPTY(70002, "商品名称不能为空"),
    
    // 评论数据相关
    COMMENT_NOT_FOUND(70003, "评论不存在"),
    COMMENT_ALREADY_EXISTS(70004, "评论已存在，无法重复评价"),
    
    // 评论权限相关
    COMMENT_PERMISSION_DENIED(70005, "无权删除他人的评论"),
    
    // 评论关联数据相关
    COMMENT_ORDER_ITEM_NOT_EXIST(70006, "订单明细不存在，无法评论"),
    COMMENT_GOODS_NOT_EXIST(70007, "商品不存在，无法评论"),
    COMMENT_USER_NOT_EXIST(70008, "用户不存在，无法评论"),

    // ==================== 文件上传相关错误码 (80xxx) ====================
    // 文件验证相关
    FILE_EMPTY(80000, "文件不能为空"),
    FILE_SIZE_EXCEEDS_LIMIT(80001, "文件大小不能超过5MB"),
    FILE_EXTENSION_NOT_ALLOWED(80002, "只允许jpg和png格式"),
    FILE_MIME_TYPE_INVALID(80003, "文件类型不合法"),
    
    // 文件操作相关
    FILE_SAVE_FAILED(80004, "文件保存失败"),
    UPLOAD_DIR_CREATE_FAILED(80005, "无法创建上传目录"),
    
    // ==================== 权限和店铺相关错误码 (500xx) ====================
    NO_PERMISSION(500014, "无权访问"),
    STORE_NOT_EXIST(500015, "店铺不存在"),
    MERCHANT_NO_SHOP(500016, "商家没有关联这个店铺"),
    INSUFFICIENT_PERMISSIONS(500017, "权限不足"),
    INVALID_ROLE(500018, "无效的用户角色"),

    // ==================== 资源相关错误码 (85xxx) ====================
    INVALID_RESOURCE_TYPE(85000, "无效的资源类型"),

    // ==================== 消息相关错误码 (91xxx) ====================
    INVALID_MESSAGE_STATUS(91000, "无效的消息状态"),
    INVALID_MESSAGE_TYPE(91001, "无效的消息类型"),
    CONVERSATION_NOT_EXIST(91002, "会话不存在"),
    MESSAGE_CONTENT_EMPTY(91003, "消息内容不能为空"),

    // ==================== 审核相关错误码 (90xxx) ====================
    AUDIT_SUBMIT_PARAMS_INCOMPLETE(90000, "审核申请参数不完整"),
    USER_NOT_LOGGED_IN_FOR_AUDIT_SUBMIT(90001, "用户未登录，无法提交审核申请"),
    AUDIT_SUBMIT_FAILED(90002, "审核申请提交失败"),
    AUDIT_DECISION_PARAMS_INCOMPLETE(90003, "审核决定参数不完整"),
    AUDIT_LOG_NOT_EXISTS(90004, "审核记录不存在"),
    AUDIT_ONLY_PENDING(90005, "只能审核待审核状态的记录"),
    AUDITOR_NOT_LOGGED_IN(90006, "审核员未登录"),
    AUDIT_ID_CANNOT_BE_NULL(90007, "审核记录ID不能为空"),
    AUDIT_WITHDRAW_OWN_ONLY(90008, "只能撤回自己提交的审核申请"),
    AUDIT_WITHDRAW_ONLY_PENDING(90009, "只能撤回待审核状态的审核申请"),
    AUDIT_EXTRA_INFO_EMPTY(90010, "审核记录扩展信息为空"),
    AUDIT_EXTRA_INFO_INVALID(90011, "审核记录扩展信息无效"),
    AUDIT_TARGET_TYPE_MISMATCH(90012, "审核对象类型不匹配"),
    AUDIT_ONLY_APPROVED(90013, "只能操作已通过的审核记录"),
    AUDIT_INVALID_STATUS(90014, "非法的审核状态"),
    UNSUPPORTED_AUDIT_TYPE(90015, "不支持的审核对象类型"),
    AUDIT_NOT_EXIST(90016, "审核批次不存在"),
    AUDIT_ITEM_NOT_FOUND(90017, "审核项目不存在"),
    AUDITOR_NOT_FOUND(90018, "审核器未找到"),

    // ==================== 秒杀相关错误码 (92xxx) ====================
    SECKILL_ACTIVITY_NOT_EXIST(92000, "秒杀活动不存在"),
    SECKILL_NOT_STARTED(92001, "秒杀活动还未开始"),
    SECKILL_ALREADY_ENDED(92002, "秒杀活动已结束"),
    SECKILL_STOCK_INSUFFICIENT(92003, "秒杀商品库存不足"),
    SECKILL_RATE_LIMIT_EXCEEDED(92004, "你的操作过于频繁，请稍后再试"),
    SECKILL_ORDER_NOT_EXIST(92005, "秒杀订单不存在"),
    SECKILL_ORDER_NOT_BELONG_TO_USER(92006, "秒杀订单不属于当前用户"),
    SECKILL_ORDER_CANNOT_CANCEL(92007, "该秒杀订单无法取消"),
    SECKILL_QUANTITY_INVALID(92008, "秒杀购买数量无效"),
    SECKILL_CACHE_INIT_FAILED(92009, "秒杀库存缓存初始化失败"),
    SECKILL_FAILED(92010, "秒杀操作失败"),
    SECKILL_ORDER_INVALID_STATUS(92011, "秒杀订单状态无效，无法执行此操作"),
    SECKILL_PRICE_MUST_LESS_THAN_ORIGINAL(92012, "秒杀价格必须低于原价"),
    SECKILL_STOCK_EXCEEDS_PRODUCT_STOCK(92013, "秒杀库存不能超过商品库存"),
    ACTIVITY_MUST_ADVANCE_24_HOURS(92014, "秒杀活动必须提前24小时以上"),
    ACTIVITY_DURATION_MUST_BE_ONE_HOUR(92015, "秒杀活动时长必须为1小时"),
    GOODS_ALREADY_IN_SECKILL_ACTIVITY(92016, "该商品已参加秒杀活动，无法重复参加"),
    PRODUCT_NOT_ACTIVE(92017, "商品未上架，无法参加秒杀"),
    INVALID_TIME_RANGE(92018, "开始时间必须早于结束时间"),
    PRODUCT_ID_REQUIRED(92019, "商品ID不能为空"),
    START_TIME_REQUIRED(92020, "开始时间不能为空"),
    END_TIME_REQUIRED(92021, "结束时间不能为空"),
    PRICE_REQUIRED(92022, "秒杀价格不能为空"),
    STOCK_REQUIRED(92023, "秒杀库存不能为空"),
    PRODUCT_NOT_FOUND(92024, "商品不存在"),
    ACTIVITY_ID_REQUIRED(92025, "活动ID不能为空"),
    INVALID_ACTIVITY_STATUS(92026, "活动状态无效，仅报名中的活动可参加"),
    ACTIVITY_MAX_ITEMS_REACHED(92027, "该活动已达到最大商品数限制"),
    ITEMS_REQUIRED(92028, "商品列表不能为空"),
    ITEMS_EMPTY(92029, "商品列表不能为空，至少需要1个商品"),
    SECKILL_GOODS_NOT_FOUND(92030, "秒杀商品不存在"),
    SECKILL_REPEAT_ORDER(92031, "同一商品不可重复秒杀"),

    // ==================== 优惠券相关错误码 (93xxx) ====================
    COUPON_NOT_EXIST(93000, "优惠券不存在"),
    COUPON_NOT_AVAILABLE(93001, "优惠券不可用"),
    COUPON_EXPIRED(93002, "优惠券已过期"),
    COUPON_ALREADY_USED(93003, "优惠券已使用"),
    COUPON_NOT_MEET_MIN_AMOUNT(93004, "未满足最低消费金额"),
    COUPON_NOT_APPLICABLE(93005, "优惠券不适用于当前商品"),
    COUPON_REACH_LIMIT(93006, "已达到领取上限"),
    COUPON_STOCK_NOT_ENOUGH(93007, "优惠券库存不足"),
    COUPON_TEMPLATE_NOT_ACTIVE(93008, "优惠券模板未生效"),
    COUPON_CLAIM_FAILED(93009, "领取优惠券失败"),
    COUPON_ALREADY_CLAIMED(93010, "已领取过该优惠券"),
    COUPON_TYPE_INVALID(93011, "无效的优惠券类型"),
    REFUND_ORDER_FAILED(93012, "退款失败")
    ;

    final int code;
    final String errorMessage;

    BizErrorCode(int code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
