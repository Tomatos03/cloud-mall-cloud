package com.cloudmall.im.security;

import com.cloudmall.im.security.filter.IMTokenAuthenticationFilter;
import com.cloudmall.im.security.handler.CustomerAccessDeniedHandler;
import com.cloudmall.im.security.handler.CustomerAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 安全链路配置
 *
 * @author : Tomatos
 * @date : 2026/2/2
 */
@Configuration
@RequiredArgsConstructor
public class SecurityChainConfig {
    private final CustomerAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomerAccessDeniedHandler accessDeniedHandler;
    private final IMTokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.authorizeHttpRequests((authorize) -> {
                               authorize.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers("/chat/ws")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated();
                           })
                           .exceptionHandling(exceptionHand -> {
                               exceptionHand.accessDeniedHandler(accessDeniedHandler);
                               exceptionHand.authenticationEntryPoint(authenticationEntryPoint);
                           })
                           .sessionManagement(session -> {
                               session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                           })
                           .cors(cors -> {
                               cors.configurationSource(corsConfigurationSource());
                           })
                           .addFilterAfter(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                           .csrf(AbstractHttpConfigurer::disable)
                           .build();
    }
}
