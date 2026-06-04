package com.douyin.config;

import com.douyin.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Spring Security 配置 — JWT 无状态认证
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    /**
     * 密码编码器 — BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（前后端分离 + JWT 不需要）
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 跨域（使用 CorsConfigurationSource）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 请求授权
                .authorizeHttpRequests(auth -> auth
                        // 静态资源公开访问
                        .requestMatchers("/uploads/**").permitAll()
                        // 公开接口（无需登录）
                        .requestMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/videos/**").permitAll()
                        .requestMatchers("/api/v1/videos/*/view").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/videos/**/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                        // 其余接口需要认证
                        .anyRequest().authenticated()
                )
                // JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JWT 认证过滤器 — 从请求头中提取 Token，设置 SecurityContext
     */
    @Bean
    public OncePerRequestFilter jwtAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String token = extractToken(request);
                if (token != null && jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    // 设置 SecurityContext，使 Spring Security 认为已认证
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    // 同时存入 request attribute，方便 Controller 直接获取
                    request.setAttribute("userId", userId);
                }
                filterChain.doFilter(request, response);
            }

            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                String path = request.getServletPath();
                // 注册和登录接口不需要 JWT 鉴权
                return path.equals("/api/v1/users/register") ||
                       path.equals("/api/v1/users/login");
            }
        };
    }

    /**
     * 从请求头中提取 Bearer Token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 跨域配置
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");      // 允许所有来源
        config.addAllowedMethod("*");              // 允许所有 HTTP 方法
        config.addAllowedHeader("*");              // 允许所有请求头
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
