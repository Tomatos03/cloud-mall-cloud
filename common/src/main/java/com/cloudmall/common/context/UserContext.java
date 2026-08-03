package com.cloudmall.common.context;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private Long userId;
    private String username;
    private String userType;
    private Long shopId;
    private List<String> permissionCodes;
    private Map<String, Object> extra;
}
