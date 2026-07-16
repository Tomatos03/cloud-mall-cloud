package com.cloudmall.im.security;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 解析后的 Token 信息
 *
 * @author : Tomatos
 * @date : 2025/12/31
 */
@Data
@AllArgsConstructor
public class ParsedToken {
    private Long userId;
    private String username;
    private List<String> roles;
    private Long storeId;
    private String accountType;
}