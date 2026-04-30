package com.lixin.capability.wechat.pay.client;

import com.lixin.capability.wechat.pay.dto.JsapiPrepayRequest;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayResponse;
import com.lixin.capability.wechat.pay.dto.RefundRequest;
import com.lixin.capability.wechat.pay.dto.RefundResponse;

public interface WechatPayClient {
    JsapiPrepayResponse jsapiPrepay(JsapiPrepayRequest request);
    RefundResponse refund(RefundRequest request);
}
