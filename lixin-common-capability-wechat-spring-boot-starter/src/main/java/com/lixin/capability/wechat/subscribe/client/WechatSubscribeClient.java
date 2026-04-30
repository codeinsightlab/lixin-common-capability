package com.lixin.capability.wechat.subscribe.client;

import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendRequest;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendResponse;

public interface WechatSubscribeClient {
    SubscribeMessageSendResponse send(SubscribeMessageSendRequest request);
}
