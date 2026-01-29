package com.ghtransport.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS("00000", "成功"),

    /**
     * 参数错误
     */
    PARAM_ERROR("A0001", "参数错误"),
    PARAM_IS_NULL("A0002", "参数为空"),
    PARAM_TYPE_ERROR("A0003", "参数类型错误"),
    PARAM_VALID_ERROR("A0004", "参数校验失败"),

    /**
     * 业务错误
     */
    BUSINESS_ERROR("B0001", "业务处理失败"),
    DATA_NOT_FOUND("B0002", "数据不存在"),
    DATA_ALREADY_EXISTS("B0003", "数据已存在"),
    OPERATION_NOT_ALLOWED("B0004", "操作不允许"),
    STATUS_ERROR("B0005", "状态异常"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR("C0001", "系统错误"),
    SERVICE_UNAVAILABLE("C0002", "服务不可用"),
    GATEWAY_ERROR("C0003", "网关错误"),
    TIMEOUT_ERROR("C0004", "超时错误"),

    /**
     * 认证授权错误
     */
    UNAUTHORIZED("D0001", "未登录或Token已过期"),
    ACCESS_DENIED("D0002", "无权限访问"),
    TOKEN_INVALID("D0003", "Token无效"),
    TOKEN_EXPIRED("D0004", "Token已过期"),

    /**
     * 限流熔断错误
     */
    FLOW_LIMIT("E0001", "请求过于频繁，请稍后重试"),
    CIRCUIT_BREAKER("E0002", "服务熔断，请稍后重试"),

    /**
     * 分布式事务错误
     */
    TRANSACTION_ERROR("F0001", "分布式事务处理失败"),
    TRANSACTION_TIMEOUT("F0002", "事务超时");

    /**
     * 响应码
     */
    private final String code;

    /**
     * 响应消息
     */
    private final String message;
}
