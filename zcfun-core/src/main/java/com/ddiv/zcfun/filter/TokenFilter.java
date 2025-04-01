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


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String token = header.substring(7);
            JWT jwt = JWTUtil.parseToken(token);
            String username = (String) jwt.getPayload().getClaim("username");

            // 检查Redis中是否存在该Token（实现单设备登录）
            String redisKey = "user:token:" + username;
            String storedToken = (String) redisTemplate.opsForValue().get(redisKey);
            UsernamePasswordAuthenticationToken authentication;
            if (storedToken != null && storedToken.equals(token)) {
                UserDetails user = userDetailsServiceImpl.loadUserByUsername(username);
                authentication =
                        new UsernamePasswordAuthenticationToken(
                                user, user.getPassword(), user.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
                return;
            }
            //redis中不存在：判断token是否有效
            //有效则存入并通过
            if (JWTUtil.verify(token, keyBytes)) {
                UserDetails user = userDetailsServiceImpl.loadUserByUsername(username);
                authentication =
                        new UsernamePasswordAuthenticationToken(
                                user, user.getPassword(), user.getAuthorities()
                        );
                redisTemplate.opsForValue().set(redisKey, token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } else {
                throw new BadCredentialsException("Token无效");
            }
        } catch (AuthenticationException e) {
            // 触发 AuthenticationEntryPoint
            jwtAuthEntryPoint.commence(request, response, e);
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
    }
}
