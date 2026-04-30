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
import com.wechat.pay.java.service.payments.model.Transaction.TradeStateEnum;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import com.wechat.pay.java.service.payments.model.TransactionPayer;
import com.wechat.pay.java.service.refund.model.Amount;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultWechatPayNotifyClientTest {
    private WechatPayNotifyAdapter notifyAdapter;
    private DefaultWechatPayNotifyClient client;

    @BeforeEach
    void setUp() {
        notifyAdapter = mock(WechatPayNotifyAdapter.class);
        client = new DefaultWechatPayNotifyClient(notifyAdapter);
    }

    @Test
    void parsePaymentNotifyRejectsNullRequest() {
        assertThatThrownBy(() -> client.parsePaymentNotify(null))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void parsePaymentNotifyRejectsBlankSerialNumber() {
        WechatNotifyRequest request = validRequest();
        request.setSerialNumber(" ");

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("serialNumber");
    }

    @Test
    void parsePaymentNotifyRejectsBlankNonce() {
        WechatNotifyRequest request = validRequest();
        request.setNonce(" ");

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("nonce");
    }

    @Test
    void parsePaymentNotifyRejectsBlankTimestamp() {
        WechatNotifyRequest request = validRequest();
        request.setTimestamp(" ");

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("timestamp");
    }

    @Test
    void parsePaymentNotifyRejectsBlankSignature() {
        WechatNotifyRequest request = validRequest();
        request.setSignature(" ");

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("signature");
    }

    @Test
    void parsePaymentNotifyRejectsBlankBody() {
        WechatNotifyRequest request = validRequest();
        request.setBody(" ");

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("body");
    }

    @Test
    void parsePaymentNotifyConvertsRequestAndResponse() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parsePaymentNotify(request)).thenReturn(validResult());

        PaymentNotifyResponse response = client.parsePaymentNotify(request);

        ArgumentCaptor<WechatNotifyRequest> captor = ArgumentCaptor.forClass(WechatNotifyRequest.class);
        verify(notifyAdapter).parsePaymentNotify(captor.capture());
        assertThat(captor.getValue().getSerialNumber()).isEqualTo("serial-1");
        assertThat(captor.getValue().getNonce()).isEqualTo("nonce-1");
        assertThat(captor.getValue().getTimestamp()).isEqualTo("1770000000");
        assertThat(captor.getValue().getSignature()).isEqualTo("signature-1");
        assertThat(captor.getValue().getBody()).isEqualTo("{\"id\":\"event-id-1\"}");

        assertThat(response.getEventId()).isEqualTo("event-id-1");
        assertThat(response.getEventType()).isEqualTo("TRANSACTION.SUCCESS");
        assertThat(response.getResourceType()).isEqualTo("encrypt-resource");
        assertThat(response.getSummary()).isEqualTo("payment success");
        assertThat(response.getAppId()).isEqualTo("app-id-1");
        assertThat(response.getMchId()).isEqualTo("mch-id-1");
        assertThat(response.getOutTradeNo()).isEqualTo("out-trade-no-1");
        assertThat(response.getTransactionId()).isEqualTo("transaction-id-1");
        assertThat(response.getTradeState()).isEqualTo("SUCCESS");
        assertThat(response.getTradeStateDesc()).isEqualTo("payment success");
        assertThat(response.getPayerOpenId()).isEqualTo("openid-1");
        assertThat(response.getAmountTotal()).isEqualTo(100);
        assertThat(response.getSuccessTime()).isEqualTo("2026-04-30T14:00:00+08:00");
        assertThat(response.getAttach()).isEqualTo("attach-data");
        assertThat(response.getRawPlaintext()).isEqualTo("{\"transaction_id\":\"transaction-id-1\"}");
    }

    @Test
    void parsePaymentNotifyConvertsVerifyFailure() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parsePaymentNotify(request)).thenThrow(new ValidationException("signature verification failed"));

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityNotifyVerifyException.class)
                .hasMessageContaining("signature verification failed");
    }

    @Test
    void parsePaymentNotifyConvertsDecryptFailure() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parsePaymentNotify(request)).thenThrow(new DecryptionException("decrypt failed", new RuntimeException("bad cipher")));

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityNotifyDecryptException.class)
                .hasMessageContaining("decryption failed");
    }

    @Test
    void parsePaymentNotifyConvertsMalformedMessage() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parsePaymentNotify(request)).thenThrow(new MalformedMessageException("malformed"));

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("parse failed");
    }

    @Test
    void parsePaymentNotifyConvertsJsonSyntaxFailure() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parsePaymentNotify(request)).thenThrow(new JsonSyntaxException("bad json"));

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("parse failed");
    }

    @Test
    void parsePaymentNotifyConvertsOtherSdkFailure() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parsePaymentNotify(request)).thenThrow(new WechatPayException("sdk failed") { });

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("SDK failed");
    }

    @Test
    void parsePaymentNotifyRejectsNullAdapterResult() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parsePaymentNotify(request)).thenReturn(null);

        assertThatThrownBy(() -> client.parsePaymentNotify(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("parse result");
    }

    @Test
    void parsePaymentNotifyRejectsBlankEventId() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getNotification().setId(" ");
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("eventId");
    }

    @Test
    void parsePaymentNotifyRejectsBlankEventType() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getNotification().setEventType(" ");
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("eventType");
    }

    @Test
    void parsePaymentNotifyRejectsBlankAppId() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getTransaction().setAppid(" ");
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("appId");
    }

    @Test
    void parsePaymentNotifyRejectsBlankMchId() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getTransaction().setMchid(" ");
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("mchId");
    }

    @Test
    void parsePaymentNotifyRejectsBlankOutTradeNo() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getTransaction().setOutTradeNo(" ");
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("outTradeNo");
    }

    @Test
    void parsePaymentNotifyRejectsBlankTransactionId() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getTransaction().setTransactionId(" ");
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("transactionId");
    }

    @Test
    void parsePaymentNotifyRejectsMissingTradeState() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getTransaction().setTradeState(null);
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("tradeState");
    }

    @Test
    void parsePaymentNotifyRejectsMissingAmountTotal() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getTransaction().setAmount(new TransactionAmount());
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("amountTotal");
    }

    @Test
    void parsePaymentNotifyRejectsInvalidAmountTotal() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getTransaction().getAmount().setTotal(0);
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("amountTotal");
    }

    @Test
    void parsePaymentNotifyRejectsBlankPayerOpenId() {
        WechatPayPaymentNotifyResult result = validResult();
        result.getTransaction().getPayer().setOpenid(" ");
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("payerOpenId");
    }

    @Test
    void parsePaymentNotifyRejectsBlankRawPlaintext() {
        WechatPayPaymentNotifyResult result = new WechatPayPaymentNotifyResult(notification(), transaction(), " ");
        when(notifyAdapter.parsePaymentNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parsePaymentNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("rawPlaintext");
    }

    @Test
    void parseRefundNotifyRejectsNullRequest() {
        assertThatThrownBy(() -> client.parseRefundNotify(null))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void parseRefundNotifyRejectsBlankSerialNumber() {
        WechatNotifyRequest request = validRequest();
        request.setSerialNumber(" ");

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("serialNumber");
    }

    @Test
    void parseRefundNotifyRejectsBlankNonce() {
        WechatNotifyRequest request = validRequest();
        request.setNonce(" ");

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("nonce");
    }

    @Test
    void parseRefundNotifyRejectsBlankTimestamp() {
        WechatNotifyRequest request = validRequest();
        request.setTimestamp(" ");

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("timestamp");
    }

    @Test
    void parseRefundNotifyRejectsBlankSignature() {
        WechatNotifyRequest request = validRequest();
        request.setSignature(" ");

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("signature");
    }

    @Test
    void parseRefundNotifyRejectsBlankBody() {
        WechatNotifyRequest request = validRequest();
        request.setBody(" ");

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("body");
    }

    @Test
    void parseRefundNotifyConvertsRequestAndResponse() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parseRefundNotify(request)).thenReturn(validRefundResult());

        RefundNotifyResponse response = client.parseRefundNotify(request);

        ArgumentCaptor<WechatNotifyRequest> captor = ArgumentCaptor.forClass(WechatNotifyRequest.class);
        verify(notifyAdapter).parseRefundNotify(captor.capture());
        assertThat(captor.getValue().getSerialNumber()).isEqualTo("serial-1");
        assertThat(captor.getValue().getNonce()).isEqualTo("nonce-1");
        assertThat(captor.getValue().getTimestamp()).isEqualTo("1770000000");
        assertThat(captor.getValue().getSignature()).isEqualTo("signature-1");
        assertThat(captor.getValue().getBody()).isEqualTo("{\"id\":\"event-id-1\"}");

        assertThat(response.getEventId()).isEqualTo("event-id-1");
        assertThat(response.getEventType()).isEqualTo("REFUND.SUCCESS");
        assertThat(response.getOutTradeNo()).isEqualTo("out-trade-no-1");
        assertThat(response.getTransactionId()).isEqualTo("transaction-id-1");
        assertThat(response.getOutRefundNo()).isEqualTo("out-refund-no-1");
        assertThat(response.getRefundId()).isEqualTo("refund-id-1");
        assertThat(response.getRefundStatus()).isEqualTo("SUCCESS");
        assertThat(response.getRefundAmount()).isEqualTo(50);
        assertThat(response.getTotalAmount()).isEqualTo(100);
        assertThat(response.getSuccessTime()).isEqualTo("2026-04-30T14:30:00+08:00");
        assertThat(response.getRawPlaintext()).isEqualTo("{\"refund_id\":\"refund-id-1\"}");
    }

    @Test
    void parseRefundNotifyAllowsBlankTransactionId() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getRefundNotification().setTransactionId(" ");
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        RefundNotifyResponse response = client.parseRefundNotify(validRequest());

        assertThat(response.getTransactionId()).isEqualTo(" ");
    }

    @Test
    void parseRefundNotifyConvertsVerifyFailure() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parseRefundNotify(request)).thenThrow(new ValidationException("signature verification failed"));

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityNotifyVerifyException.class)
                .hasMessageContaining("signature verification failed");
    }

    @Test
    void parseRefundNotifyConvertsDecryptFailure() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parseRefundNotify(request)).thenThrow(new DecryptionException("decrypt failed", new RuntimeException("bad cipher")));

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityNotifyDecryptException.class)
                .hasMessageContaining("decryption failed");
    }

    @Test
    void parseRefundNotifyConvertsMalformedMessage() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parseRefundNotify(request)).thenThrow(new MalformedMessageException("malformed"));

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("parse failed");
    }

    @Test
    void parseRefundNotifyConvertsJsonSyntaxFailure() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parseRefundNotify(request)).thenThrow(new JsonSyntaxException("bad json"));

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("parse failed");
    }

    @Test
    void parseRefundNotifyConvertsOtherSdkFailure() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parseRefundNotify(request)).thenThrow(new WechatPayException("sdk failed") { });

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("SDK failed");
    }

    @Test
    void parseRefundNotifyRejectsNullAdapterResult() {
        WechatNotifyRequest request = validRequest();
        when(notifyAdapter.parseRefundNotify(request)).thenReturn(null);

        assertThatThrownBy(() -> client.parseRefundNotify(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("parse result");
    }

    @Test
    void parseRefundNotifyRejectsBlankEventId() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getNotification().setId(" ");
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("eventId");
    }

    @Test
    void parseRefundNotifyRejectsBlankEventType() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getNotification().setEventType(" ");
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("eventType");
    }

    @Test
    void parseRefundNotifyRejectsBlankOutTradeNo() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getRefundNotification().setOutTradeNo(" ");
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("outTradeNo");
    }

    @Test
    void parseRefundNotifyRejectsBlankOutRefundNo() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getRefundNotification().setOutRefundNo(" ");
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("outRefundNo");
    }

    @Test
    void parseRefundNotifyRejectsBlankRefundId() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getRefundNotification().setRefundId(" ");
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("refundId");
    }

    @Test
    void parseRefundNotifyRejectsMissingRefundStatus() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getRefundNotification().setRefundStatus(null);
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("refundStatus");
    }

    @Test
    void parseRefundNotifyRejectsMissingRefundAmount() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getRefundNotification().getAmount().setRefund(null);
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("refundAmount");
    }

    @Test
    void parseRefundNotifyRejectsInvalidRefundAmount() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getRefundNotification().getAmount().setRefund(0L);
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("refundAmount");
    }

    @Test
    void parseRefundNotifyRejectsMissingTotalAmount() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getRefundNotification().getAmount().setTotal(null);
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("totalAmount");
    }

    @Test
    void parseRefundNotifyRejectsInvalidTotalAmount() {
        WechatPayRefundNotifyResult result = validRefundResult();
        result.getRefundNotification().getAmount().setTotal(0L);
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("totalAmount");
    }

    @Test
    void parseRefundNotifyRejectsBlankRawPlaintext() {
        WechatPayRefundNotifyResult result = new WechatPayRefundNotifyResult(refundNotificationEvent(), refundNotification(), " ");
        when(notifyAdapter.parseRefundNotify(any(WechatNotifyRequest.class))).thenReturn(result);

        assertThatThrownBy(() -> client.parseRefundNotify(validRequest()))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("rawPlaintext");
    }

    private WechatNotifyRequest validRequest() {
        WechatNotifyRequest request = new WechatNotifyRequest();
        request.setSerialNumber("serial-1");
        request.setNonce("nonce-1");
        request.setTimestamp("1770000000");
        request.setSignature("signature-1");
        request.setBody("{\"id\":\"event-id-1\"}");
        return request;
    }

    private WechatPayPaymentNotifyResult validResult() {
        return new WechatPayPaymentNotifyResult(notification(), transaction(), "{\"transaction_id\":\"transaction-id-1\"}");
    }

    private WechatPayRefundNotifyResult validRefundResult() {
        return new WechatPayRefundNotifyResult(refundNotificationEvent(), refundNotification(), "{\"refund_id\":\"refund-id-1\"}");
    }

    private Notification notification() {
        Notification notification = new Notification();
        notification.setId("event-id-1");
        notification.setEventType("TRANSACTION.SUCCESS");
        notification.setResourceType("encrypt-resource");
        notification.setSummary("payment success");
        return notification;
    }

    private Transaction transaction() {
        Transaction transaction = new Transaction();
        transaction.setAppid("app-id-1");
        transaction.setMchid("mch-id-1");
        transaction.setOutTradeNo("out-trade-no-1");
        transaction.setTransactionId("transaction-id-1");
        transaction.setTradeState(TradeStateEnum.SUCCESS);
        transaction.setTradeStateDesc("payment success");
        transaction.setSuccessTime("2026-04-30T14:00:00+08:00");
        transaction.setAttach("attach-data");

        TransactionAmount amount = new TransactionAmount();
        amount.setTotal(100);
        transaction.setAmount(amount);

        TransactionPayer payer = new TransactionPayer();
        payer.setOpenid("openid-1");
        transaction.setPayer(payer);
        return transaction;
    }

    private Notification refundNotificationEvent() {
        Notification notification = new Notification();
        notification.setId("event-id-1");
        notification.setEventType("REFUND.SUCCESS");
        notification.setResourceType("encrypt-resource");
        notification.setSummary("refund success");
        return notification;
    }

    private RefundNotification refundNotification() {
        RefundNotification refund = new RefundNotification();
        refund.setOutTradeNo("out-trade-no-1");
        refund.setTransactionId("transaction-id-1");
        refund.setOutRefundNo("out-refund-no-1");
        refund.setRefundId("refund-id-1");
        refund.setRefundStatus(Status.SUCCESS);
        refund.setSuccessTime("2026-04-30T14:30:00+08:00");

        Amount amount = new Amount();
        amount.setRefund(50L);
        amount.setTotal(100L);
        refund.setAmount(amount);
        return refund;
    }
}
