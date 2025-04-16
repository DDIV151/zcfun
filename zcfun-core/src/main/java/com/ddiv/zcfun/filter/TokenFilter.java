package com.ddiv.zcfun.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.ddiv.zcfun.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TokenFilter extends OncePerRequestFilter {

    private final static byte[] keyBytes = "ee044392420d89b41081a267f3672935".getBytes();
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthenticationEntryPoint jwtAuthEntryPoint;

    public TokenFilter(UserDetailsServiceImpl userDetailsServiceImpl, RedisTemplate<String, Object> redisTemplate, AuthenticationEntryPoint jwtAuthEntryPoint) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.redisTemplate = redisTemplate;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }


    /**
     * 该方法是过滤器链的核心处理逻辑，用于处理HTTP请求中的JWT令牌验证和用户认证。
     *
     * @param request     HttpServletRequest对象，包含客户端的请求信息。
     * @param response    HttpServletResponse对象，用于向客户端发送响应。
     * @param filterChain FilterChain对象，用于将请求传递给下一个过滤器或目标资源。
     * @throws ServletException 如果处理请求时发生Servlet相关的异常。
     * @throws IOException      如果处理请求时发生I/O相关的异常。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中获取Authorization字段，用于提取JWT令牌
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            // 如果请求头中没有有效的Bearer令牌，则直接放行请求
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // 提取并解析JWT令牌
            String token = header.substring(7);
            JWT jwt = JWTUtil.parseToken(token);
            String username = (String) jwt.getPayload().getClaim("username");

            // 检查Redis中是否存在该Token
            String redisKey = "user:token:" + username;
            String storedToken = (String) redisTemplate.opsForValue().get(redisKey);
            UsernamePasswordAuthenticationToken authentication;
            if (storedToken != null && storedToken.equals(token)) {
                // 如果Redis中存在该Token，则加载用户信息并设置认证上下文
                UserDetails user = userDetailsServiceImpl.loadUserByUsername(username);
                authentication =
                        new UsernamePasswordAuthenticationToken(
                                user, user.getPassword(), user.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
                return;
            }
            // 如果Redis中不存在该Token，则验证Token的有效性
            if (JWTUtil.verify(token, keyBytes)) {
                // 如果Token有效，则加载用户信息并设置认证上下文，同时将Token存入Redis
                UserDetails user = userDetailsServiceImpl.loadUserByUsername(username);
                authentication =
                        new UsernamePasswordAuthenticationToken(
                                user, user.getPassword(), user.getAuthorities()
                        );
                redisTemplate.opsForValue().set(redisKey, token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } else {
                // 如果Token无效，则抛出BadCredentialsException异常
                throw new BadCredentialsException("Token无效");
            }
        } catch (AuthenticationException e) {
            // 处理认证异常，触发AuthenticationEntryPoint并清除安全上下文
            jwtAuthEntryPoint.commence(request, response, e);
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            // 处理其他异常，清除安全上下文
            SecurityContextHolder.clearContext();
        }
    }
}
