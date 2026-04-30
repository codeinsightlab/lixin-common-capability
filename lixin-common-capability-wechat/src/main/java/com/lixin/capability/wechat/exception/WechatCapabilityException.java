package com.lixin.capability.wechat.exception;

public class WechatCapabilityException extends RuntimeException {
    public WechatCapabilityException(String message) { super(message); }
    public WechatCapabilityException(String message, Throwable cause) { super(message, cause); }
}
