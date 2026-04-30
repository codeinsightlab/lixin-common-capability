package com.lixin.capability.wechat.subscribe.client;

import com.lixin.capability.wechat.exception.WechatCapabilityException;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendRequest;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendResponse;

public class DefaultWechatSubscribeClient implements WechatSubscribeClient {
    @Override
    public SubscribeMessageSendResponse send(SubscribeMessageSendRequest request) {
        throw new WechatCapabilityException("Real WeChat subscribe message API call is not implemented yet.");
    }
}
