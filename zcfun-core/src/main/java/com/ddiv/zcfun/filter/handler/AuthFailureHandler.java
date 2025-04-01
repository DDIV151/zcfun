package com.ddiv.zcfun.filter.handler;

import cn.hutool.json.JSONUtil;
import com.ddiv.zcfun.domain.ApiResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthFailureHandler implements AuthenticationFailureHandler {

    /**
     * 自定义处理器，旨在处理登录失败时异常，到达此处说明生成token被中断
     * @param request the request during which the authentication attempt occurred.
     * @param response the response.
     * @param exception the exception which was thrown to reject the authentication
     * request.
     * @throws IOException 预料外
     * @throws ServletException
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //响应json
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(
                    JSONUtil.toJsonStr(ApiResult.error(401, "用户名或密码错误"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
