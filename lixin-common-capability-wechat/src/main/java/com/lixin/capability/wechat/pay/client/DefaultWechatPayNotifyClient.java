package com.lixin.capability.wechat.pay.client;

import com.google.gson.JsonSyntaxException;
import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.exception.WechatCapabilityNotifyDecryptException;
import com.lixin.capability.wechat.exception.WechatCapabilityNotifyVerifyException;
import com.lixin.capability.wechat.exception.WechatCapabilityParseException;
import com.lixin.capability.wechat.pay.client.internal.WechatPayNotifyAdapter;
import com.lixin.capability.wechat.pay.client.internal.WechatPayPaymentNotifyResult;
import com.lixin.capability.wechat.pay.client.internal.WechatPayRefundNotifyResult;
import com.lixin.capability.wechat.pay.notify.PaymentNotifyResponse;
import com.lixin.capability.wechat.pay.notify.RefundNotifyResponse;
import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;
import com.wechat.pay.java.core.exception.DecryptionException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.exception.WechatPayException;
import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import com.wechat.pay.java.service.payments.model.TransactionPayer;
import com.wechat.pay.java.service.refund.model.Amount;
import com.wechat.pay.java.service.refund.model.RefundNotification;

public class DefaultWechatPayNotifyClient implements WechatPayNotifyClient {
    private final WechatPayNotifyAdapter notifyAdapter;

    public DefaultWechatPayNotifyClient() {
        this(null);
    }

    public DefaultWechatPayNotifyClient(WechatPayNotifyAdapter notifyAdapter) {
        this.notifyAdapter = notifyAdapter;
    }

    @Override
    public PaymentNotifyResponse parsePaymentNotify(WechatNotifyRequest request) {
        validatePaymentNotify(request);
        if (notifyAdapter == null) {
            throw new WechatCapabilityInvalidRequestException("WechatPayNotifyAdapter must not be null.");
        }
        try {
            WechatPayPaymentNotifyResult result = notifyAdapter.parsePaymentNotify(request);
            return toPaymentNotifyResponse(result);
        } catch (ValidationException e) {
            throw new WechatCapabilityNotifyVerifyException("WeChat Pay payment notify signature verification failed. " + e.getMessage(), e);
        } catch (DecryptionException e) {
            throw new WechatCapabilityNotifyDecryptException("WeChat Pay payment notify decryption failed. " + e.getMessage(), e);
        } catch (MalformedMessageException | JsonSyntaxException e) {
            throw new WechatCapabilityParseException("WeChat Pay payment notify parse failed. " + e.getMessage(), e);
        } catch (WechatCapabilityException e) {
            throw e;
        } catch (WechatPayException e) {
            throw new WechatCapabilityApiException(null, "WeChat Pay payment notify SDK failed. " + e.getMessage(), null, e);
        } catch (RuntimeException e) {
            throw new WechatCapabilityApiException(null, "WeChat Pay payment notify SDK failed. " + e.getMessage(), null, e);
        }
    }

    @Override
    public RefundNotifyResponse parseRefundNotify(WechatNotifyRequest request) {
        validateRefundNotify(request);
        if (notifyAdapter == null) {
            throw new WechatCapabilityInvalidRequestException("WechatPayNotifyAdapter must not be null.");
        }
        try {
            WechatPayRefundNotifyResult result = notifyAdapter.parseRefundNotify(request);
            return toRefundNotifyResponse(result);
        } catch (ValidationException e) {
            throw new WechatCapabilityNotifyVerifyException("WeChat Pay refund notify signature verification failed. " + e.getMessage(), e);
        } catch (DecryptionException e) {
            throw new WechatCapabilityNotifyDecryptException("WeChat Pay refund notify decryption failed. " + e.getMessage(), e);
        } catch (MalformedMessageException | JsonSyntaxException e) {
            throw new WechatCapabilityParseException("WeChat Pay refund notify parse failed. " + e.getMessage(), e);
        } catch (WechatCapabilityException e) {
            throw e;
        } catch (WechatPayException e) {
            throw new WechatCapabilityApiException(null, "WeChat Pay refund notify SDK failed. " + e.getMessage(), null, e);
        } catch (RuntimeException e) {
            throw new WechatCapabilityApiException(null, "WeChat Pay refund notify SDK failed. " + e.getMessage(), null, e);
        }
    }

    private PaymentNotifyResponse toPaymentNotifyResponse(WechatPayPaymentNotifyResult result) {
        if (result == null) {
            throw new WechatCapabilityParseException("WeChat Pay payment notify parse result must not be null.");
        }
        Notification notification = result.getNotification();
        Transaction transaction = result.getTransaction();
        if (notification == null) {
            throw new WechatCapabilityParseException("WeChat Pay payment notify event must not be null.");
        }
        if (transaction == null) {
            throw new WechatCapabilityParseException("WeChat Pay payment notify transaction must not be null.");
        }
        requireField(notification.getId(), "eventId");
        requireField(notification.getEventType(), "eventType");
        requireField(notification.getResourceType(), "resourceType");
        requireField(transaction.getAppid(), "appId");
        requireField(transaction.getMchid(), "mchId");
        requireField(transaction.getOutTradeNo(), "outTradeNo");
        requireField(transaction.getTransactionId(), "transactionId");
        if (transaction.getTradeState() == null) {
            throw new WechatCapabilityParseException("WeChat Pay payment notify tradeState must not be blank.");
        }
        TransactionAmount amount = transaction.getAmount();
        if (amount == null || amount.getTotal() == null || amount.getTotal() <= 0) {
            throw new WechatCapabilityParseException("WeChat Pay payment notify amountTotal must be greater than 0.");
        }
        TransactionPayer payer = transaction.getPayer();
        if (payer == null || isBlank(payer.getOpenid())) {
            throw new WechatCapabilityParseException("WeChat Pay payment notify payerOpenId must not be blank.");
        }
        requireField(result.getRawPlaintext(), "rawPlaintext");

        PaymentNotifyResponse response = new PaymentNotifyResponse();
        response.setEventId(notification.getId());
        response.setEventType(notification.getEventType());
        response.setResourceType(notification.getResourceType());
        response.setSummary(notification.getSummary());
        response.setAppId(transaction.getAppid());
        response.setMchId(transaction.getMchid());
        response.setOutTradeNo(transaction.getOutTradeNo());
        response.setTransactionId(transaction.getTransactionId());
        response.setTradeState(transaction.getTradeState().name());
        response.setTradeStateDesc(transaction.getTradeStateDesc());
        response.setPayerOpenId(payer.getOpenid());
        response.setAmountTotal(amount.getTotal());
        response.setSuccessTime(transaction.getSuccessTime());
        response.setAttach(transaction.getAttach());
        response.setRawPlaintext(result.getRawPlaintext());
        return response;
    }

    private RefundNotifyResponse toRefundNotifyResponse(WechatPayRefundNotifyResult result) {
        if (result == null) {
            throw new WechatCapabilityParseException("WeChat Pay refund notify parse result must not be null.");
        }
        Notification notification = result.getNotification();
        RefundNotification refund = result.getRefundNotification();
        if (notification == null) {
            throw new WechatCapabilityParseException("WeChat Pay refund notify event must not be null.");
        }
        if (refund == null) {
            throw new WechatCapabilityParseException("WeChat Pay refund notify detail must not be null.");
        }
        requireRefundField(notification.getId(), "eventId");
        requireRefundField(notification.getEventType(), "eventType");
        requireRefundField(refund.getOutTradeNo(), "outTradeNo");
        requireRefundField(refund.getOutRefundNo(), "outRefundNo");
        requireRefundField(refund.getRefundId(), "refundId");
        if (refund.getRefundStatus() == null) {
            throw new WechatCapabilityParseException("WeChat Pay refund notify refundStatus must not be blank.");
        }
        Amount amount = refund.getAmount();
        if (amount == null || amount.getRefund() == null || amount.getRefund() <= 0) {
            throw new WechatCapabilityParseException("WeChat Pay refund notify refundAmount must be greater than 0.");
        }
        if (amount.getTotal() == null || amount.getTotal() <= 0) {
            throw new WechatCapabilityParseException("WeChat Pay refund notify totalAmount must be greater than 0.");
        }
        requireRefundField(result.getRawPlaintext(), "rawPlaintext");

        RefundNotifyResponse response = new RefundNotifyResponse();
        response.setEventId(notification.getId());
        response.setEventType(notification.getEventType());
        response.setOutTradeNo(refund.getOutTradeNo());
        response.setTransactionId(refund.getTransactionId());
        response.setOutRefundNo(refund.getOutRefundNo());
        response.setRefundId(refund.getRefundId());
        response.setRefundStatus(refund.getRefundStatus().name());
        response.setRefundAmount(amount.getRefund().intValue());
        response.setTotalAmount(amount.getTotal().intValue());
        response.setSuccessTime(refund.getSuccessTime());
        response.setRawPlaintext(result.getRawPlaintext());
        return response;
    }

    private void validatePaymentNotify(WechatNotifyRequest request) {
        if (request == null) {
            throw new WechatCapabilityInvalidRequestException("Payment notify request must not be null.");
        }
        if (isBlank(request.getSerialNumber())) {
            throw new WechatCapabilityInvalidRequestException("Payment notify serialNumber must not be blank.");
        }
        if (isBlank(request.getNonce())) {
            throw new WechatCapabilityInvalidRequestException("Payment notify nonce must not be blank.");
        }
        if (isBlank(request.getTimestamp())) {
            throw new WechatCapabilityInvalidRequestException("Payment notify timestamp must not be blank.");
        }
        if (isBlank(request.getSignature())) {
            throw new WechatCapabilityInvalidRequestException("Payment notify signature must not be blank.");
        }
        if (isBlank(request.getBody())) {
            throw new WechatCapabilityInvalidRequestException("Payment notify body must not be blank.");
        }
    }

    private void validateRefundNotify(WechatNotifyRequest request) {
        if (request == null) {
            throw new WechatCapabilityInvalidRequestException("Refund notify request must not be null.");
        }
        if (isBlank(request.getSerialNumber())) {
            throw new WechatCapabilityInvalidRequestException("Refund notify serialNumber must not be blank.");
        }
        if (isBlank(request.getNonce())) {
            throw new WechatCapabilityInvalidRequestException("Refund notify nonce must not be blank.");
        }
        if (isBlank(request.getTimestamp())) {
            throw new WechatCapabilityInvalidRequestException("Refund notify timestamp must not be blank.");
        }
        if (isBlank(request.getSignature())) {
            throw new WechatCapabilityInvalidRequestException("Refund notify signature must not be blank.");
        }
        if (isBlank(request.getBody())) {
            throw new WechatCapabilityInvalidRequestException("Refund notify body must not be blank.");
        }
    }

    private void requireField(String value, String fieldName) {
        if (isBlank(value)) {
            throw new WechatCapabilityParseException("WeChat Pay payment notify " + fieldName + " must not be blank.");
        }
    }

    private void requireRefundField(String value, String fieldName) {
        if (isBlank(value)) {
            throw new WechatCapabilityParseException("WeChat Pay refund notify " + fieldName + " must not be blank.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
