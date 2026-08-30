package com.cloudmall.product.enums;

/**
 * 评论排序方式。
 *
 * @author : Tomatos
 */
public enum CommentSortEnum {
    /** 默认（综合：好评优先 + 时间倒序） */
    DEFAULT,

    /** 最新（时间倒序） */
    LATEST,

    /** 最热（有用数倒序 + 时间倒序） */
    HOTTEST
}
