package com.lixin.capability.wechat.pay.client.internal;

import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.service.payments.model.Transaction;

public class WechatPayPaymentNotifyResult {
    private final Notification notification;
    private final Transaction transaction;
    private final String rawPlaintext;

    public WechatPayPaymentNotifyResult(Notification notification, Transaction transaction, String rawPlaintext) {
        this.notification = notification;
        this.transaction = transaction;
        this.rawPlaintext = rawPlaintext;
    }

    public Notification getNotification() {
        return notification;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public String getRawPlaintext() {
        return rawPlaintext;
    }
}
