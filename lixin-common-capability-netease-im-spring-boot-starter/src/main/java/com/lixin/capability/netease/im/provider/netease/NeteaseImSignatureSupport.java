package com.lixin.capability.netease.im.provider.netease;

import com.lixin.capability.netease.im.exception.NeteaseImCallbackVerifyException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class NeteaseImSignatureSupport {
    public String requestCheckSum(String appSecret, String nonce, String curTime) {
        return digest("SHA-1", appSecret + nonce + curTime);
    }

    public String callbackCheckSum(String appSecret, String bodyMd5, String curTime) {
        return digest("SHA-1", appSecret + bodyMd5 + curTime);
    }

    public String md5(String body) {
        return digest("MD5", body);
    }

    private String digest(String algorithm, String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(value.getBytes(StandardCharsets.UTF_8));
            return toLowerHex(messageDigest.digest());
        } catch (Exception e) {
            throw new NeteaseImCallbackVerifyException("Netease IM signature calculation failed", e);
        }
    }

    private String toLowerHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        char[] digits = "0123456789abcdef".toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            hex[i * 2] = digits[value >>> 4];
            hex[i * 2 + 1] = digits[value & 0x0f];
        }
        return new String(hex);
    }
}
