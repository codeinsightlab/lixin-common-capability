package com.lixin.capability.wechat.pay.client.internal;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

public interface WechatPayJsapiAdapter {
    PrepayWithRequestPaymentResponse prepay(PrepayRequest request);
}
