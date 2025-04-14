package com.ddiv.zcfun.exception;

import com.ddiv.zcfun.domain.ApiResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    private final HttpServletResponse httpServletResponse;

    public GlobalExceptionHandler(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResult handleMaxUploadSizeExceededException() {
        return ApiResult.error(400500, "上传文件大小超过限制");
    }

    @ExceptionHandler(Exception.class)
    public ApiResult handleException(Exception e) throws Exception {
        if (e instanceof AccessDeniedException) {
            //放行@hasRole抛出的异常,由Security的AccessDeniedHandler处理
            throw e;
        }
        httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return ApiResult.error(500, "服务器异常");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResult handleNoResourceFoundException() {
        return ApiResult.error(404, "资源不存在");
    }

    @ExceptionHandler(UserRegisterException.class)
    public ApiResult handleUserRegisterException(UserRegisterException e) {
        return ApiResult.error(401, e.getMessage());
    }
}
