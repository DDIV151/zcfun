package com.ddiv.zcfun.domain;

import lombok.Data;


@Data
public class ApiResult {


    @Data
    private static class BaseResult {
        private int code;
        private String msg;

    }

    private BaseResult base = new BaseResult();
    private Object data;

    public static ApiResult success(Object data) {
        ApiResult result = new ApiResult();
        result.getBase().setCode(10000);
        result.getBase().setMsg("success");
        result.setData(data);
        return result;
    }

    public static ApiResult success() {
        return success(null);
    }

    public static ApiResult error(Integer code, String msg, Object data) {
        ApiResult result = new ApiResult();
        result.getBase().setCode(code);
        result.getBase().setMsg(msg);
        result.setData(data);
        return result;
    }

    public static ApiResult error(Integer code, String msg) {
        return error(code, msg, null);
    }
}
