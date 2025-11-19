package com.example.seatingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;


@Configuration
@EnableWebSecurity // 启用 Spring Security 的 Web 安全功能
public class SecurityConfig {

    /**
     * 配置密码编码器 (已在 UserServiceImpl 中使用)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置安全过滤链，定义权限规则
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 授权配置：使用 Lambda 表达式和新的方法
                .authorizeHttpRequests(authorize -> authorize
                        // 允许所有人访问的公共路径
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        // 所有其他请求都需要认证（登录）
                        .anyRequest().authenticated()
                )
                // 2. 表单登录配置：禁用 Spring Security 默认的登录表单
                .formLogin(form -> form.disable())

                // 3. CSRF 配置 (暂时禁用方便测试)
                .csrf(csrf -> csrf.disable())

                // 4. Session 管理（设置无效 Session 跳转）
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login")
                );

        return http.build();
    }
}