package com.ddiv.zcfun.configuration;

import com.ddiv.zcfun.filter.JsonLoginFilter;
import com.ddiv.zcfun.filter.TokenFilter;
import com.ddiv.zcfun.filter.handler.AuthFailureHandler;
import com.ddiv.zcfun.filter.handler.AuthSuccessHandler;
import com.ddiv.zcfun.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenFilter tokenFilter, JsonLoginFilter jsonLoginFilter, AuthenticationEntryPoint jwtAuthEntryPoint, AccessDeniedHandler jwtAccessDeniedHandler) throws Exception {
        http
                //禁用 CSRF 和 Session
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //忽略注册和登录
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/register").permitAll()
                        .requestMatchers("/user/login").permitAll()
                        .requestMatchers("/ws/**").permitAll()  // 允许WebSocket连接路径
                        .anyRequest().authenticated()
                )
                //登录过滤器
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class)
                // 异常处理
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                //CORS 配置
                .cors(Customizer.withDefaults());
        return http.build();
    }


    //认证异常处理器
    @Bean
    AuthenticationEntryPoint jwtAuthEntryPoint() {
        return (request, response, ex) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("""
                        {"code": 401, "msg": "需要认证"}
                    """);
        };
    }

    //权限异常处理器
    @Bean
    AccessDeniedHandler jwtAccessDeniedHandler() {
        return (request, response, ex) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("""
                        {"code": 403, "msg": "无权访问该资源"}
                    """);
        };
    }

    //登录验证器
    @Bean
    JsonLoginFilter jsonLoginFilter(AuthenticationManager authenticationManager, AuthSuccessHandler authSuccessHandler, AuthFailureHandler authFailureHandler) {
        JsonLoginFilter filter = new JsonLoginFilter();
        //限定
        filter.setFilterProcessesUrl("/user/login");
        filter.setAuthenticationManager(authenticationManager);
        //分别设置成功与失败时处理器
        filter.setAuthenticationSuccessHandler(authSuccessHandler);
        filter.setAuthenticationFailureHandler(authFailureHandler);
        return filter;
    }

    //token验证（身份验证过滤器）
    @Bean
    TokenFilter tokenFilter(RedisTemplate<String, Object> redisTemplate, AuthenticationEntryPoint jwtAuthEntryPoint, UserDetailsServiceImpl userDetailsService) {
        return new TokenFilter(userDetailsService, redisTemplate, jwtAuthEntryPoint);
    }

    //处理用户形象的获得，下面都是
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder, UserDetailsServiceImpl userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        //加密
        return new BCryptPasswordEncoder();
    }
}