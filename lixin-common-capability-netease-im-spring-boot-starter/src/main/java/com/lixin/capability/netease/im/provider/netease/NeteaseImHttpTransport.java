package com.lixin.capability.netease.im.provider.netease;

import java.util.Map;

public interface NeteaseImHttpTransport {
    NeteaseImHttpResponse postForm(String url, Map<String, String> headers, Map<String, String> form, int timeoutMillis);
}
