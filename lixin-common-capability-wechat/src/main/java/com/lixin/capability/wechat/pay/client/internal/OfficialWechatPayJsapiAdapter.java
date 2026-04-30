package com.lixin.capability.wechat.pay.client.internal;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

public class OfficialWechatPayJsapiAdapter implements WechatPayJsapiAdapter {
    private final JsapiServiceExtension jsapiService;

    public OfficialWechatPayJsapiAdapter(Config config) {
        this(new JsapiServiceExtension.Builder().config(config).build());
    }

    public OfficialWechatPayJsapiAdapter(JsapiServiceExtension jsapiService) {
        if (jsapiService == null) {
            throw new IllegalArgumentException("JsapiServiceExtension must not be null.");
        }
        this.jsapiService = jsapiService;
    }

    @Override
    public PrepayWithRequestPaymentResponse prepay(PrepayRequest request) {
        return jsapiService.prepayWithRequestPayment(request);
    }
}
