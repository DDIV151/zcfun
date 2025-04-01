package com.ddiv.zcfun.filter.handler;

import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.ddiv.zcfun.domain.ApiResult;
import com.ddiv.zcfun.domain.LoginUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${jwt.key}")
    private static String key;

    /**
     * 登录成功处理器，经过验证的用户到达（登录成功）后返回token
     * <p>
     * 1.token存入redis
     * </p>
     * 2.返回token
     *
     * @param request        the request which caused the successful authentication
     * @param response       the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     *                       the authentication process.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //生成token
        LoginUser user = (LoginUser) authentication.getPrincipal();
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", user.getUser().getUserId());
        payload.put("username", user.getUsername());
        String token = JWTUtil.createToken(payload, key.getBytes());
        //token存入redis
        redisTemplate.opsForValue().set("user:token:" + user.getUsername(), token);
        //响应json
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(
                    JSONUtil.toJsonStr(ApiResult.success(token))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
