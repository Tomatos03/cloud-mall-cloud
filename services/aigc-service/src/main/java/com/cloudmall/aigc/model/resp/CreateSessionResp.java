package com.cloudmall.aigc.model.resp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionResp {
    private String sessionId;
    private String title;
    private String describe;
    private List<HotTopicResp> examples;
}
