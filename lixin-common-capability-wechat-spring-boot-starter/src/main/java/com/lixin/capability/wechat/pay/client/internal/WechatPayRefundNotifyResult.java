package com.lixin.capability.wechat.pay.client.internal;

import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.service.refund.model.RefundNotification;

public class WechatPayRefundNotifyResult {
    private final Notification notification;
    private final RefundNotification refundNotification;
    private final String rawPlaintext;

    public WechatPayRefundNotifyResult(Notification notification, RefundNotification refundNotification, String rawPlaintext) {
        this.notification = notification;
        this.refundNotification = refundNotification;
        this.rawPlaintext = rawPlaintext;
    }

    public Notification getNotification() {
        return notification;
    }

    public RefundNotification getRefundNotification() {
        return refundNotification;
    }

    public String getRawPlaintext() {
        return rawPlaintext;
    }
}
