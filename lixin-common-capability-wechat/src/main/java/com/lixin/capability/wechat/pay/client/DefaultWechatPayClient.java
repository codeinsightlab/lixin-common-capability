package com.lixin.capability.wechat.pay.client;

import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.pay.client.internal.WechatPayJsapiAdapter;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayRequest;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayResponse;
import com.lixin.capability.wechat.pay.dto.RefundRequest;
import com.lixin.capability.wechat.pay.dto.RefundResponse;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.WechatPayException;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

public class DefaultWechatPayClient implements WechatPayClient {
    private final String appId;
    private final String mchId;
    private final String defaultNotifyUrl;
    private final WechatPayJsapiAdapter jsapiAdapter;

    public DefaultWechatPayClient(String appId, String mchId, String defaultNotifyUrl, WechatPayJsapiAdapter jsapiAdapter) {
        this.appId = appId;
        this.mchId = mchId;
        this.defaultNotifyUrl = defaultNotifyUrl;
        this.jsapiAdapter = jsapiAdapter;
    }

    @Override
    public JsapiPrepayResponse jsapiPrepay(JsapiPrepayRequest request) {
        validateJsapiPrepay(request);
        PrepayRequest sdkRequest = buildPrepayRequest(request);
        try {
            PrepayWithRequestPaymentResponse sdkResponse = jsapiAdapter.prepay(sdkRequest);
            return toResponse(sdkResponse);
        } catch (ServiceException e) {
            throw new WechatCapabilityApiException(e.getErrorCode(),
                    "WeChat Pay JSAPI prepay failed. " + e.getErrorMessage(), e.getResponseBody(), e);
        } catch (WechatPayException e) {
            throw new WechatCapabilityApiException(null,
                    "WeChat Pay JSAPI prepay failed. " + e.getMessage(), null, e);
        } catch (RuntimeException e) {
            throw new WechatCapabilityApiException(null,
                    "WeChat Pay JSAPI prepay failed. " + e.getMessage(), null, e);
        }
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        throw notImplemented();
    }

    private PrepayRequest buildPrepayRequest(JsapiPrepayRequest request) {
        PrepayRequest sdkRequest = new PrepayRequest();
        sdkRequest.setAppid(appId);
        sdkRequest.setMchid(mchId);
        sdkRequest.setDescription(request.getDescription());
        sdkRequest.setOutTradeNo(request.getOutTradeNo());
        sdkRequest.setNotifyUrl(defaultString(request.getNotifyUrl(), defaultNotifyUrl));
        sdkRequest.setAttach(request.getAttach());
        sdkRequest.setTimeExpire(request.getTimeExpire());

        Amount amount = new Amount();
        amount.setTotal(request.getAmountTotal());
        amount.setCurrency(defaultString(request.getCurrency(), "CNY"));
        sdkRequest.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(request.getPayerOpenId());
        sdkRequest.setPayer(payer);
        return sdkRequest;
    }

    private JsapiPrepayResponse toResponse(PrepayWithRequestPaymentResponse sdkResponse) {
        if (sdkResponse == null) {
            throw new WechatCapabilityApiException("WeChat Pay JSAPI prepay returned empty response.");
        }
        JsapiPrepayResponse response = new JsapiPrepayResponse();
        response.setAppId(sdkResponse.getAppId());
        response.setTimeStamp(sdkResponse.getTimeStamp());
        response.setNonceStr(sdkResponse.getNonceStr());
        response.setPackageValue(sdkResponse.getPackageVal());
        response.setSignType(sdkResponse.getSignType());
        response.setPaySign(sdkResponse.getPaySign());
        response.setPrepayId(extractPrepayId(sdkResponse.getPackageVal()));
        return response;
    }

    private void validateJsapiPrepay(JsapiPrepayRequest request) {
        if (request == null) {
            throw new WechatCapabilityInvalidRequestException("JSAPI prepay request must not be null.");
        }
        if (isBlank(request.getDescription())) {
            throw new WechatCapabilityInvalidRequestException("JSAPI prepay description must not be blank.");
        }
        if (isBlank(request.getOutTradeNo())) {
            throw new WechatCapabilityInvalidRequestException("JSAPI prepay outTradeNo must not be blank.");
        }
        if (isBlank(request.getPayerOpenId())) {
            throw new WechatCapabilityInvalidRequestException("JSAPI prepay payerOpenId must not be blank.");
        }
        if (request.getAmountTotal() == null || request.getAmountTotal() <= 0) {
            throw new WechatCapabilityInvalidRequestException("JSAPI prepay amountTotal must be greater than 0.");
        }
        if (isBlank(defaultString(request.getNotifyUrl(), defaultNotifyUrl))) {
            throw new WechatCapabilityInvalidRequestException("JSAPI prepay notifyUrl must not be blank.");
        }
        if (jsapiAdapter == null) {
            throw new WechatCapabilityInvalidRequestException("WechatPayJsapiAdapter must not be null.");
        }
    }

    private String extractPrepayId(String packageValue) {
        if (isBlank(packageValue)) {
            return null;
        }
        String prefix = "prepay_id=";
        if (packageValue.startsWith(prefix)) {
            return packageValue.substring(prefix.length());
        }
        return packageValue;
    }

    private WechatCapabilityException notImplemented() {
        return new WechatCapabilityException("Real WeChat Pay refund API call is not implemented yet.");
    }

    private String defaultString(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
