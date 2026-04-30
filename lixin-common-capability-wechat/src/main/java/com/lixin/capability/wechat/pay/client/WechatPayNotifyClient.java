package com.lixin.capability.wechat.pay.client;

import com.lixin.capability.wechat.pay.notify.PaymentNotifyResponse;
import com.lixin.capability.wechat.pay.notify.RefundNotifyResponse;
import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;

public interface WechatPayNotifyClient {
    PaymentNotifyResponse parsePaymentNotify(WechatNotifyRequest request);
    RefundNotifyResponse parseRefundNotify(WechatNotifyRequest request);
}
