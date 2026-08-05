package com.cloudmall.im.model.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 消息历史分页查询请求
 *
 * @author : Tomatos
 * @date : 2026/8
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessagePageReq {

    /** 会话ID */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /** 当前页码（从 1 开始，1 表示最新一页） */
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    /** 每页数量 */
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;
}
