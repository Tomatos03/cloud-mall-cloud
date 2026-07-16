package com.cloudmall.im.model.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.cloudmall.im.common.entity.PageParamsDTO;

/**
 * 消息历史分页查询参数DTO
 *
 * @author : Tomatos
 * @date : 2026/02/02
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IMPageParamsDTO extends PageParamsDTO {
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
}