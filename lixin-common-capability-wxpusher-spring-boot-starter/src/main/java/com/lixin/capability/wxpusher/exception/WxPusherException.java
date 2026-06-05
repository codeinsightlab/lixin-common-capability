package com.lixin.capability.wxpusher.exception;

public class WxPusherException extends RuntimeException {
    public WxPusherException(String message) { super(message); }
    public WxPusherException(String message, Throwable cause) { super(message, cause); }
}
