package com.lxm.danmu.common.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {
    //通用
    SUCCESS(200, "SUCCESS"),
    ERROR(500, "服务端异常"),
    //登录模块5002xx
    LOGIN_ERROR(500210, "用户名或密码不正确"),
//    MOBILE_ERROR(500211, "手机号码格式不正确"),
    BIND_ERROR(500212, "参数校验异常"),
//    MOBILE_NOT_EXIST(500213, "手机号码不存在"),
    PASSWORD_UPDATE_FAIL(500214, "密码更新失败"),
    SESSION_ERROR(500215, "用户不存在"),
    USER_ALREADY_EXIST(500216,"用户已经存在"),

    //房间模块 5005xx
    LIVE_EXIST(500501,"房间已经存在，创建失败"),
    LIVE_NOT_EXIST(500502,"房间不存在，删除失败"),
    LIVE_HAS_MEMBER(500503,"房间内存在观众，不可删除"),
    LIVE_NO_AUTH(500504,"不是房间的所有者，不可删除");
    ;
    private final Integer code;
    private final String message;
}