package com.lixin.capability.wechat.pay.client;

import com.lixin.capability.wechat.exception.WechatCapabilityException;
import com.lixin.capability.wechat.pay.notify.PaymentNotifyResponse;
import com.lixin.capability.wechat.pay.notify.RefundNotifyResponse;
import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;

public class DefaultWechatPayNotifyClient implements WechatPayNotifyClient {
    @Override
    public PaymentNotifyResponse parsePaymentNotify(WechatNotifyRequest request) {
        throw notImplemented();
    }

    @Override
    public RefundNotifyResponse parseRefundNotify(WechatNotifyRequest request) {
        throw notImplemented();
    }

    private WechatCapabilityException notImplemented() {
        return new WechatCapabilityException("Real WeChat Pay notify verification, decryption, and parsing are not implemented yet.");
    }
}
