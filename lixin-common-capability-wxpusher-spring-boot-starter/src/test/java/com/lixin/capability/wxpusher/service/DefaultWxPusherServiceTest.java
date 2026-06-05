package com.lixin.capability.wxpusher.service;

import com.lixin.capability.wxpusher.dto.WxPusherSendRequest;
import com.lixin.capability.wxpusher.dto.WxPusherSendResult;
import com.lixin.capability.wxpusher.dto.WxPusherResponse;
import com.lixin.capability.wxpusher.exception.WxPusherApiException;
import com.lixin.capability.wxpusher.properties.WxPusherProperties;
import com.lixin.capability.wxpusher.provider.WxPusherProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultWxPusherServiceTest {
    private WxPusherProperties properties;
    private RecordingProvider provider;
    private DefaultWxPusherService service;

    @BeforeEach
    void setUp() {
        properties = new WxPusherProperties();
        properties.setEnabled(true);
        properties.setAppToken("AT_test");
        provider = new RecordingProvider();
        service = new DefaultWxPusherService(properties, provider);
    }

    @Test
    void disabledReturnsFailureAndDoesNotCallProvider() {
        properties.setEnabled(false);

        WxPusherSendResult result = service.sendToUid("UID_1", "title", "content", 1, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("disabled");
        assertThat(provider.callCount).isZero();
    }

    @Test
    void blankAppTokenReturnsFailureAndDoesNotCallProvider() {
        properties.setAppToken(" ");

        WxPusherSendResult result = service.sendToUid("UID_1", "title", "content", 1, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("app-token");
        assertThat(provider.callCount).isZero();
    }

    @Test
    void blankUidReturnsFailureAndDoesNotCallProvider() {
        WxPusherSendResult result = service.sendToUid(" ", "title", "content", 1, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("target");
        assertThat(provider.callCount).isZero();
    }

    @Test
    void blankContentReturnsFailureAndDoesNotCallProvider() {
        WxPusherSendResult result = service.sendToUid("UID_1", "title", " ", 1, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("content");
        assertThat(provider.callCount).isZero();
    }

    @Test
    void successfulResponseMapsMessageContentIdAndSendRecordIds() {
        WxPusherSendResult result = service.sendToUid("UID_1", "title", "content", 2, "https://example.com");

        assertThat(provider.callCount).isEqualTo(1);
        assertThat(provider.request.getUids()).containsExactly("UID_1");
        assertThat(provider.request.getTitle()).isEqualTo("title");
        assertThat(provider.request.getContent()).isEqualTo("content");
        assertThat(provider.request.getContentType()).isEqualTo(2);
        assertThat(provider.request.getUrl()).isEqualTo("https://example.com");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderCode()).isEqualTo(1000);
        assertThat(result.getMessageContentId()).isEqualTo(2123);
        assertThat(result.getSendRecordIds()).containsExactly(12313L);
    }

    @Test
    void providerFailureResponseMapsToFailureResult() {
        provider.failureResponse = true;

        WxPusherSendResult result = service.sendToUid("UID_1", "title", "content", 1, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getProviderCode()).isEqualTo(1001);
        assertThat(result.getProviderMessage()).isEqualTo("发送失败");
    }

    @Test
    void providerExceptionReturnsFailureAndDoesNotThrowToCaller() {
        provider.throwApiException = true;

        WxPusherSendResult result = service.sendToUid("UID_1", "title", "content", 1, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("HTTP status 500");
        assertThat(result.getProviderCode()).isEqualTo(500);
    }

    private static class RecordingProvider implements WxPusherProvider {
        private int callCount;
        private WxPusherSendRequest request;
        private boolean throwApiException;
        private boolean failureResponse;

        @Override
        public WxPusherResponse send(WxPusherSendRequest request) {
            callCount++;
            this.request = request;
            if (throwApiException) {
                throw new WxPusherApiException("WxPusher HTTP status 500", 500, "{\"code\":500}");
            }
            WxPusherResponse.Item item = new WxPusherResponse.Item();
            item.setUid("UID_1");
            item.setMessageContentId(2123);
            item.setSendRecordId(12313L);
            item.setCode(1000);
            item.setStatus("创建发送任务成功");
            WxPusherResponse response = new WxPusherResponse();
            response.setCode(failureResponse ? 1001 : 1000);
            response.setMsg(failureResponse ? "发送失败" : "处理成功");
            response.setSuccess(!failureResponse);
            response.setRawResponse("{\"code\":1000}");
            response.setData(Collections.singletonList(item));
            return response;
        }
    }
}
