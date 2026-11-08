package com.cloudmall.user.api.model.req;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户通知与隐私设置请求，字段均为可选。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsUpdateReq {

    @Pattern(regexp = "PUBLIC|PRIVATE", message = "个人主页可见范围只能为PUBLIC或PRIVATE")
    private String profileVisibility;

    @Pattern(regexp = "PUBLIC|PRIVATE", message = "购买记录可见范围只能为PUBLIC或PRIVATE")
    private String purchaseVisibility;

    private Boolean notifyOrder;

    private Boolean notifyPromotion;

    private Boolean notifyLogistics;

    private Boolean notifyCommunity;

    private Boolean recommendEnabled;
}
