package com.example.astaroswiki.common;

import lombok.Data;

@Data
public class Result<T> {
    private static final Integer SUCCESS_CODE = 200;
    private static final Integer ERROR_CODE = 500;

    private Integer code; // 状态码
    private String msg; // 状态描述
    private T data; // 数据

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code; // 状态码
        this.msg = msg; // 状态描述
        this.data = data; // 数据
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "success", null); // 返回成功结果，无数据
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "success", data); // 返回成功结果，有数据
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(SUCCESS_CODE, msg, data); // 返回成功结果，有数据
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(ERROR_CODE, msg, null); // 返回错误结果，无数据
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null); // 返回错误结果，无数据
    }
}
