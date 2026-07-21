package com.cloudmall.agent.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReq {
    @NotBlank(message = "sessionId不能为空")
    private String sessionId;
    @NotBlank(message = "prompt不能为空")
    private String prompt;
}
