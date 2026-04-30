package com.lixin.capability.wechat.pay.client;

import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.exception.WechatCapabilityParseException;
import com.lixin.capability.wechat.pay.client.internal.WechatPayJsapiAdapter;
import com.lixin.capability.wechat.pay.client.internal.WechatPayRefundAdapter;
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
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;

public class DefaultWechatPayClient implements WechatPayClient {
    private final String appId;
    private final String mchId;
    private final String defaultNotifyUrl;
    private final String defaultRefundNotifyUrl;
    private final WechatPayJsapiAdapter jsapiAdapter;
    private final WechatPayRefundAdapter refundAdapter;

    public DefaultWechatPayClient(String appId, String mchId, String defaultNotifyUrl, WechatPayJsapiAdapter jsapiAdapter) {
        this(appId, mchId, defaultNotifyUrl, null, jsapiAdapter, null);
    }

    public DefaultWechatPayClient(String appId, String mchId, String defaultNotifyUrl,
                                  String defaultRefundNotifyUrl,
                                  WechatPayJsapiAdapter jsapiAdapter,
                                  WechatPayRefundAdapter refundAdapter) {
        this.appId = appId;
        this.mchId = mchId;
        this.defaultNotifyUrl = defaultNotifyUrl;
        this.defaultRefundNotifyUrl = defaultRefundNotifyUrl;
        this.jsapiAdapter = jsapiAdapter;
        this.refundAdapter = refundAdapter;
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
        } catch (WechatCapabilityException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new WechatCapabilityApiException(null,
                    "WeChat Pay JSAPI prepay failed. " + e.getMessage(), null, e);
        }
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        validateRefund(request);
        CreateRequest sdkRequest = buildRefundRequest(request);
        try {
            Refund sdkResponse = refundAdapter.create(sdkRequest);
            return toRefundResponse(sdkResponse);
        } catch (ServiceException e) {
            throw new WechatCapabilityApiException(e.getErrorCode(),
                    "WeChat Pay refund failed. " + e.getErrorMessage(), e.getResponseBody(), e);
        } catch (WechatPayException e) {
            throw new WechatCapabilityApiException(null,
                    "WeChat Pay refund failed. " + e.getMessage(), null, e);
        } catch (WechatCapabilityException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new WechatCapabilityApiException(null,
                    "WeChat Pay refund failed. " + e.getMessage(), null, e);
        }
    }

    private CreateRequest buildRefundRequest(RefundRequest request) {
        CreateRequest sdkRequest = new CreateRequest();
        sdkRequest.setOutTradeNo(request.getOutTradeNo());
        sdkRequest.setTransactionId(request.getTransactionId());
        sdkRequest.setOutRefundNo(request.getOutRefundNo());
        sdkRequest.setReason(request.getReason());
        String notifyUrl = defaultString(request.getNotifyUrl(), defaultRefundNotifyUrl);
        if (!isBlank(notifyUrl)) {
            sdkRequest.setNotifyUrl(notifyUrl);
        }

        AmountReq amount = new AmountReq();
        amount.setRefund(request.getRefundAmount().longValue());
        amount.setTotal(request.getTotalAmount().longValue());
        amount.setCurrency(defaultString(request.getCurrency(), "CNY"));
        sdkRequest.setAmount(amount);
        return sdkRequest;
    }

    private RefundResponse toRefundResponse(Refund sdkResponse) {
        if (sdkResponse == null) {
            throw new WechatCapabilityApiException("WeChat Pay refund returned empty response.");
        }
        requireRefundResponseField(sdkResponse.getRefundId(), "refundId");
        requireRefundResponseField(sdkResponse.getOutRefundNo(), "outRefundNo");
        if (sdkResponse.getStatus() == null) {
            throw new WechatCapabilityParseException("WeChat Pay refund response status must not be blank.");
        }

        RefundResponse response = new RefundResponse();
        response.setRefundId(sdkResponse.getRefundId());
        response.setOutRefundNo(sdkResponse.getOutRefundNo());
        response.setTransactionId(sdkResponse.getTransactionId());
        response.setOutTradeNo(sdkResponse.getOutTradeNo());
        response.setStatus(sdkResponse.getStatus().name());
        return response;
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
        requireResponseField(sdkResponse.getAppId(), "appId");
        requireResponseField(sdkResponse.getTimeStamp(), "timeStamp");
        requireResponseField(sdkResponse.getNonceStr(), "nonceStr");
        requireResponseField(sdkResponse.getPackageVal(), "packageVal");
        requireResponseField(sdkResponse.getSignType(), "signType");
        requireResponseField(sdkResponse.getPaySign(), "paySign");

        String prepayId = extractPrepayId(sdkResponse.getPackageVal());
        JsapiPrepayResponse response = new JsapiPrepayResponse();
        response.setAppId(sdkResponse.getAppId());
        response.setTimeStamp(sdkResponse.getTimeStamp());
        response.setNonceStr(sdkResponse.getNonceStr());
        response.setPackageValue(sdkResponse.getPackageVal());
        response.setSignType(sdkResponse.getSignType());
        response.setPaySign(sdkResponse.getPaySign());
        response.setPrepayId(prepayId);
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

    private void validateRefund(RefundRequest request) {
        if (request == null) {
            throw new WechatCapabilityInvalidRequestException("Refund request must not be null.");
        }
        if (isBlank(request.getOutRefundNo())) {
            throw new WechatCapabilityInvalidRequestException("Refund outRefundNo must not be blank.");
        }
        if (isBlank(request.getOutTradeNo()) && isBlank(request.getTransactionId())) {
            throw new WechatCapabilityInvalidRequestException("Refund outTradeNo or transactionId must not be blank.");
        }
        if (request.getRefundAmount() == null) {
            throw new WechatCapabilityInvalidRequestException("Refund refundAmount must not be null.");
        }
        if (request.getRefundAmount() <= 0) {
            throw new WechatCapabilityInvalidRequestException("Refund refundAmount must be greater than 0.");
        }
        if (request.getTotalAmount() == null) {
            throw new WechatCapabilityInvalidRequestException("Refund totalAmount must not be null.");
        }
        if (request.getTotalAmount() <= 0) {
            throw new WechatCapabilityInvalidRequestException("Refund totalAmount must be greater than 0.");
        }
        if (request.getRefundAmount() > request.getTotalAmount()) {
            throw new WechatCapabilityInvalidRequestException("Refund refundAmount must not be greater than totalAmount.");
        }
        if (refundAdapter == null) {
            throw new WechatCapabilityInvalidRequestException("WechatPayRefundAdapter must not be null.");
        }
    }

    private String extractPrepayId(String packageValue) {
        if (isBlank(packageValue)) {
            throw new WechatCapabilityParseException("WeChat Pay JSAPI prepay packageVal must not be blank.");
        }
        String prefix = "prepay_id=";
        if (!packageValue.startsWith(prefix)) {
            throw new WechatCapabilityParseException("WeChat Pay JSAPI prepay packageVal must use prepay_id=xxx format.");
        }
        String prepayId = packageValue.substring(prefix.length());
        if (isBlank(prepayId)) {
            throw new WechatCapabilityParseException("WeChat Pay JSAPI prepay prepayId must not be blank.");
        }
        return prepayId;
    }

    private void requireResponseField(String value, String fieldName) {
        if (isBlank(value)) {
            throw new WechatCapabilityParseException("WeChat Pay JSAPI prepay response " + fieldName + " must not be blank.");
        }
    }

    private void requireRefundResponseField(String value, String fieldName) {
        if (isBlank(value)) {
            throw new WechatCapabilityParseException("WeChat Pay refund response " + fieldName + " must not be blank.");
        }
    }

    private String defaultString(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
