package com.lixin.capability.wechat.pay.client.internal;

import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;

public interface WechatPayRefundAdapter {
    Refund create(CreateRequest request);
}
