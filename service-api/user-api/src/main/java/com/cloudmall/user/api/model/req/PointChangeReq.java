package com.cloudmall.user.api.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部积分变动请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointChangeReq {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "积分变动值不能为空")
    private Integer changeAmount;

    @NotBlank(message = "积分变动类型不能为空")
    private String type;

    private String description;
}
