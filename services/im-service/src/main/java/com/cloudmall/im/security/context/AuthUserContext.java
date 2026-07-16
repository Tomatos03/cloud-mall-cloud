package com.cloudmall.im.security.context;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cloudmall.im.security.ParsedToken;

/**
 * 认证用户上下文
 *
 * @author : Tomatos
 * @date : 2026/2/3
 */
public final class AuthUserContext {

    private AuthUserContext() {
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static Long getUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ParsedToken) {
            return ((ParsedToken) principal).getUserId();
        }
        return null;
    }

    public static String getUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ParsedToken) {
            return ((ParsedToken) principal).getUsername();
        }
        return authentication.getName();
    }

    public static Long getStoreId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ParsedToken) {
            return ((ParsedToken) principal).getStoreId();
        }
        return null;
    }

    public static void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}