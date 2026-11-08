package com.cloudmall.im.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 买家商家聊天会话实体
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_im_session")
public class ChatSessionDO implements Serializable {

    /** 会话ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 买家用户ID */
    private Long buyerId;

    /** 商家用户ID */
    private Long merchantId;

    /** 买家是否置顶 */
    private Boolean buyerPinned;

    /** 商家是否置顶 */
    private Boolean merchantPinned;

    /** 买家是否隐藏会话 */
    private Boolean buyerHidden;

    /** 商家是否隐藏会话 */
    private Boolean merchantHidden;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;

    /**
     * 判断用户是否为会话成员。
     *
     * @param userId 用户ID
     * @return true 表示用户是买家或商家成员
     */
    public boolean isMember(Long userId) {
        return isBuyer(userId) || isMerchant(userId);
    }

    /**
     * 判断用户是否为买家。
     *
     * @param userId 用户ID
     * @return true 表示用户是买家
     */
    public boolean isBuyer(Long userId) {
        return userId != null && userId.equals(buyerId);
    }

    /**
     * 判断用户是否为商家。
     *
     * @param userId 用户ID
     * @return true 表示用户是商家
     */
    public boolean isMerchant(Long userId) {
        return userId != null && userId.equals(merchantId);
    }

    /**
     * 判断会话是否被指定用户置顶。
     *
     * @param userId 用户ID
     * @return true 表示当前用户已置顶该会话
     */
    public boolean isPinnedBy(Long userId) {
        if (isBuyer(userId)) {
            return BooleanUtil.isTrue(buyerPinned);
        }
        if (isMerchant(userId)) {
            return BooleanUtil.isTrue(merchantPinned);
        }
        return false;
    }

    /**
     * 判断会话是否被指定用户隐藏。
     *
     * @param userId 用户ID
     * @return true 表示当前用户已隐藏该会话
     */
    public boolean isHiddenBy(Long userId) {
        if (isBuyer(userId)) {
            return BooleanUtil.isTrue(buyerHidden);
        }
        if (isMerchant(userId)) {
            return BooleanUtil.isTrue(merchantHidden);
        }
        return false;
    }
}
