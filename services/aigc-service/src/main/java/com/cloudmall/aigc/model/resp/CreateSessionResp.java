package com.cloudmall.aigc.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
