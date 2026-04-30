package com.lixin.capability.wechat.pay.client.internal;

import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;

public interface WechatPayNotifyAdapter {
    WechatPayPaymentNotifyResult parsePaymentNotify(WechatNotifyRequest request);
    WechatPayRefundNotifyResult parseRefundNotify(WechatNotifyRequest request);
}
