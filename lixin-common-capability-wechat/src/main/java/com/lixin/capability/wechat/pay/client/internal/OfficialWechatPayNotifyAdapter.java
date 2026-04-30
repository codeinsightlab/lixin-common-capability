package com.lixin.capability.wechat.pay.client.internal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;
import com.wechat.pay.java.core.notification.Constant;
import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;

public class OfficialWechatPayNotifyAdapter implements WechatPayNotifyAdapter {
    private final NotificationParser notificationParser;
    private final Gson gson = new Gson();

    public OfficialWechatPayNotifyAdapter(NotificationParser notificationParser) {
        if (notificationParser == null) {
            throw new IllegalArgumentException("NotificationParser must not be null.");
        }
        this.notificationParser = notificationParser;
    }

    @Override
    public WechatPayPaymentNotifyResult parsePaymentNotify(WechatNotifyRequest request) {
        JsonObject plaintextObject = parsePlaintextObject(request);
        String rawPlaintext = gson.toJson(plaintextObject);
        Transaction transaction = gson.fromJson(plaintextObject, Transaction.class);
        return new WechatPayPaymentNotifyResult(parseNotification(request), transaction, rawPlaintext);
    }

    @Override
    public WechatPayRefundNotifyResult parseRefundNotify(WechatNotifyRequest request) {
        JsonObject plaintextObject = parsePlaintextObject(request);
        String rawPlaintext = gson.toJson(plaintextObject);
        RefundNotification refundNotification = gson.fromJson(plaintextObject, RefundNotification.class);
        return new WechatPayRefundNotifyResult(parseNotification(request), refundNotification, rawPlaintext);
    }

    private JsonObject parsePlaintextObject(WechatNotifyRequest request) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(request.getSerialNumber())
                .nonce(request.getNonce())
                .timestamp(request.getTimestamp())
                .signature(request.getSignature())
                .signType(Constant.RSA_SIGN_TYPE)
                .body(request.getBody())
                .build();
        return notificationParser.parse(requestParam, JsonObject.class);
    }

    private Notification parseNotification(WechatNotifyRequest request) {
        return gson.fromJson(request.getBody(), Notification.class);
    }
}
