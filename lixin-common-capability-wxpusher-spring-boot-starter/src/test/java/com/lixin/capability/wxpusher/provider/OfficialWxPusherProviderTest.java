package com.lixin.capability.wxpusher.provider;

import com.lixin.capability.wxpusher.dto.WxPusherSendRequest;
import com.lixin.capability.wxpusher.dto.WxPusherResponse;
import com.smjcco.wxpusher.client.sdk.bean.Message;
import com.smjcco.wxpusher.client.sdk.bean.MessageResult;
import com.smjcco.wxpusher.client.sdk.bean.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfficialWxPusherProviderTest {
    private RecordingSdkGateway sdkGateway;
    private OfficialWxPusherProvider provider;

    @BeforeEach
    void setUp() {
        sdkGateway = new RecordingSdkGateway();
        provider = new OfficialWxPusherProvider(sdkGateway);
    }

    @Test
    void buildsSdkMessageFields() {
        WxPusherSendRequest request = new WxPusherSendRequest();
        request.setUids(Arrays.asList("UID_1", "UID_2"));
        request.setTopicIds(Collections.singletonList(123));
        request.setTitle("summary");
        request.setContent("content");
        request.setContentType(2);
        request.setUrl("https://example.com");

        WxPusherResponse response = provider.send(request);

        assertThat(sdkGateway.message.getContent()).isEqualTo("content");
        assertThat(sdkGateway.message.getSummary()).isEqualTo("summary");
        assertThat(sdkGateway.message.getContentType()).isEqualTo(2);
        assertThat(sdkGateway.message.getUrl()).isEqualTo("https://example.com");
        assertThat(sdkGateway.message.getUids()).containsExactly("UID_1", "UID_2");
        assertThat(sdkGateway.message.getTopicIds()).containsExactly(123L);
        assertThat(response.getCode()).isEqualTo(1000);
        assertThat(response.getData().get(0).getUid()).isEqualTo("UID_1");
        assertThat(response.getData().get(0).getMessageId()).isEqualTo(121);
    }

    @Test
    void sdkFailureCodeThrowsApiExceptionForServiceLayerToConvert() {
        sdkGateway.result.setCode(1001);
        sdkGateway.result.setMsg("发送失败");

        assertThatThrownBy(() -> provider.send(validRequest()))
                .isInstanceOf(com.lixin.capability.wxpusher.exception.WxPusherApiException.class);
    }

    private WxPusherSendRequest validRequest() {
        WxPusherSendRequest request = new WxPusherSendRequest();
        request.setUids(Collections.singletonList("UID_1"));
        request.setContent("content");
        request.setContentType(1);
        return request;
    }

    private static class RecordingSdkGateway implements WxPusherSdkGateway {
        private Message message;
        private Result<List<MessageResult>> result;

        private RecordingSdkGateway() {
            MessageResult messageResult = new MessageResult();
            messageResult.setUid("UID_1");
            messageResult.setCode(1000);
            messageResult.setStatus("创建发送任务成功");
            messageResult.setMessageId(121L);
            result = new Result<>(1000, "处理成功");
            result.setData(Collections.singletonList(messageResult));
        }

        @Override
        public Result<List<MessageResult>> send(Message message) {
            this.message = message;
            return result;
        }
    }
}
