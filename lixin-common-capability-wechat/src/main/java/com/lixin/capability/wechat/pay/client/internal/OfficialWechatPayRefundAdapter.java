package com.lixin.capability.wechat.pay.client.internal;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;

public class OfficialWechatPayRefundAdapter implements WechatPayRefundAdapter {
    private final RefundService refundService;

    public OfficialWechatPayRefundAdapter(Config config) {
        this(new RefundService.Builder().config(config).build());
    }

    public OfficialWechatPayRefundAdapter(RefundService refundService) {
        if (refundService == null) {
            throw new IllegalArgumentException("RefundService must not be null.");
        }
        this.refundService = refundService;
    }

    @Override
    public Refund create(CreateRequest request) {
        return refundService.create(request);
    }
}
