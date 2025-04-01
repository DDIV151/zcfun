package com.ddiv.zcfun.filter;

import com.ddiv.zcfun.domain.po.UserPO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.io.InputStream;

public class JsonLoginFilter extends AbstractAuthenticationProcessingFilter {

    public JsonLoginFilter() {
        super(new AntPathRequestMatcher("/user/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getContentType().equals("application/json")) {
            throw new AuthenticationServiceException("Authentication content type not supported");
        } else {
            UserPO user;
            try {
                InputStream inputStream = request.getInputStream();
                user = new ObjectMapper().readValue(inputStream, UserPO.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }
}
