package com.lixin.capability.wechat.pay.client;

import com.lixin.capability.wechat.exception.WechatCapabilityException;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayRequest;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayResponse;
import com.lixin.capability.wechat.pay.dto.RefundRequest;
import com.lixin.capability.wechat.pay.dto.RefundResponse;

public class DefaultWechatPayClient implements WechatPayClient {
    @Override
    public JsapiPrepayResponse jsapiPrepay(JsapiPrepayRequest request) {
        throw notImplemented();
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        throw notImplemented();
    }

    private WechatCapabilityException notImplemented() {
        return new WechatCapabilityException("Real WeChat Pay API call is not implemented yet.");
    }
}
