package com.cloudmall.user.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户通知与隐私设置。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResp {

    /** 个人主页可见范围：PUBLIC / PRIVATE */
    private String profileVisibility;

    /** 购买记录可见范围：PUBLIC / PRIVATE */
    private String purchaseVisibility;

    private Boolean notifyOrder;

    private Boolean notifyPromotion;

    private Boolean notifyLogistics;

    private Boolean notifyCommunity;

    private Boolean recommendEnabled;
}
