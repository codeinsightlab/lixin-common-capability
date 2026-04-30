package com.lixin.capability.wechat.pay.client;

import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.pay.client.internal.WechatPayJsapiAdapter;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayRequest;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayResponse;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultWechatPayClientTest {
    private WechatPayJsapiAdapter jsapiAdapter;
    private DefaultWechatPayClient client;

    @BeforeEach
    void setUp() {
        jsapiAdapter = mock(WechatPayJsapiAdapter.class);
        client = new DefaultWechatPayClient("app-id-1", "mch-id-1", "https://example.com/pay/notify", jsapiAdapter);
    }

    @Test
    void jsapiPrepayRejectsNullRequest() {
        assertThatThrownBy(() -> client.jsapiPrepay(null))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void jsapiPrepayRejectsBlankDescription() {
        JsapiPrepayRequest request = validRequest();
        request.setDescription(" ");

        assertThatThrownBy(() -> client.jsapiPrepay(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("description");
    }

    @Test
    void jsapiPrepayRejectsBlankOutTradeNo() {
        JsapiPrepayRequest request = validRequest();
        request.setOutTradeNo(null);

        assertThatThrownBy(() -> client.jsapiPrepay(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("outTradeNo");
    }

    @Test
    void jsapiPrepayRejectsBlankPayerOpenId() {
        JsapiPrepayRequest request = validRequest();
        request.setPayerOpenId(null);

        assertThatThrownBy(() -> client.jsapiPrepay(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("payerOpenId");
    }

    @Test
    void jsapiPrepayRejectsInvalidAmount() {
        JsapiPrepayRequest request = validRequest();
        request.setAmountTotal(0);

        assertThatThrownBy(() -> client.jsapiPrepay(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("amountTotal");
    }

    @Test
    void jsapiPrepayRejectsMissingNotifyUrl() {
        DefaultWechatPayClient noDefaultNotifyClient = new DefaultWechatPayClient("app-id-1", "mch-id-1", null, jsapiAdapter);
        JsapiPrepayRequest request = validRequest();
        request.setNotifyUrl(null);

        assertThatThrownBy(() -> noDefaultNotifyClient.jsapiPrepay(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("notifyUrl");
    }

    @Test
    void jsapiPrepayConvertsRequestAndResponse() {
        PrepayWithRequestPaymentResponse sdkResponse = sdkResponse();
        when(jsapiAdapter.prepay(any(PrepayRequest.class))).thenReturn(sdkResponse);

        JsapiPrepayRequest request = validRequest();
        request.setCurrency(null);
        request.setNotifyUrl("https://example.com/request/notify");
        JsapiPrepayResponse response = client.jsapiPrepay(request);

        ArgumentCaptor<PrepayRequest> captor = ArgumentCaptor.forClass(PrepayRequest.class);
        verify(jsapiAdapter).prepay(captor.capture());
        PrepayRequest sdkRequest = captor.getValue();
        assertThat(sdkRequest.getAppid()).isEqualTo("app-id-1");
        assertThat(sdkRequest.getMchid()).isEqualTo("mch-id-1");
        assertThat(sdkRequest.getDescription()).isEqualTo("test goods");
        assertThat(sdkRequest.getOutTradeNo()).isEqualTo("out-trade-no-1");
        assertThat(sdkRequest.getNotifyUrl()).isEqualTo("https://example.com/request/notify");
        assertThat(sdkRequest.getAttach()).isEqualTo("attach-data");
        assertThat(sdkRequest.getTimeExpire()).isEqualTo("2026-04-30T16:00:00+08:00");
        assertThat(sdkRequest.getAmount().getTotal()).isEqualTo(100);
        assertThat(sdkRequest.getAmount().getCurrency()).isEqualTo("CNY");
        assertThat(sdkRequest.getPayer().getOpenid()).isEqualTo("openid-1");

        assertThat(response.getAppId()).isEqualTo("app-id-1");
        assertThat(response.getTimeStamp()).isEqualTo("1770000000");
        assertThat(response.getNonceStr()).isEqualTo("nonce-1");
        assertThat(response.getPackageValue()).isEqualTo("prepay_id=prepay-id-1");
        assertThat(response.getSignType()).isEqualTo("RSA");
        assertThat(response.getPaySign()).isEqualTo("pay-sign-1");
        assertThat(response.getPrepayId()).isEqualTo("prepay-id-1");
    }

    @Test
    void jsapiPrepayUsesDefaultNotifyUrlWhenRequestNotifyUrlBlank() {
        when(jsapiAdapter.prepay(any(PrepayRequest.class))).thenReturn(sdkResponse());
        JsapiPrepayRequest request = validRequest();
        request.setNotifyUrl(" ");

        client.jsapiPrepay(request);

        ArgumentCaptor<PrepayRequest> captor = ArgumentCaptor.forClass(PrepayRequest.class);
        verify(jsapiAdapter).prepay(captor.capture());
        assertThat(captor.getValue().getNotifyUrl()).isEqualTo("https://example.com/pay/notify");
    }

    @Test
    void jsapiPrepayConvertsSdkException() {
        when(jsapiAdapter.prepay(any(PrepayRequest.class))).thenThrow(new ValidationException("sdk failed"));

        assertThatThrownBy(() -> client.jsapiPrepay(validRequest()))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("JSAPI prepay failed");
    }

    private JsapiPrepayRequest validRequest() {
        JsapiPrepayRequest request = new JsapiPrepayRequest();
        request.setDescription("test goods");
        request.setOutTradeNo("out-trade-no-1");
        request.setAmountTotal(100);
        request.setCurrency("CNY");
        request.setPayerOpenId("openid-1");
        request.setAttach("attach-data");
        request.setTimeExpire("2026-04-30T16:00:00+08:00");
        return request;
    }

    private PrepayWithRequestPaymentResponse sdkResponse() {
        PrepayWithRequestPaymentResponse response = new PrepayWithRequestPaymentResponse();
        response.setAppId("app-id-1");
        response.setTimeStamp("1770000000");
        response.setNonceStr("nonce-1");
        response.setPackageVal("prepay_id=prepay-id-1");
        response.setSignType("RSA");
        response.setPaySign("pay-sign-1");
        return response;
    }
}
