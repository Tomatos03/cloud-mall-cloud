package com.cloudmall.aigc.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新会话请求
 *
 * @author : Tomatos
 * @date : 2026/7/21
 */
@Data
public class SessionUpdateReq {

    @NotBlank(message = "会话标题不能为空")
    private String title;
}
