package com.cloudmall.aigc.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新会话请求
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionUpdateReq {

    @NotBlank(message = "会话标题不能为空")
    private String title;
}
