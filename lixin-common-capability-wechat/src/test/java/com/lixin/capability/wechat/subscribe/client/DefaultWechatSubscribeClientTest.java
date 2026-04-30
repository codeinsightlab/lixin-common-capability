package com.lixin.capability.wechat.subscribe.client;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaSubscribeService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageData;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendRequest;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendResponse;
import me.chanjar.weixin.common.error.WxErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultWechatSubscribeClientTest {
    private WxMaService wxMaService;
    private WxMaSubscribeService wxMaSubscribeService;
    private DefaultWechatSubscribeClient client;

    @BeforeEach
    void setUp() {
        wxMaService = mock(WxMaService.class);
        wxMaSubscribeService = mock(WxMaSubscribeService.class);
        when(wxMaService.getSubscribeService()).thenReturn(wxMaSubscribeService);
        client = new DefaultWechatSubscribeClient(wxMaService, "developer", "en_US");
    }

    @Test
    void sendRejectsNullRequest() {
        assertThatThrownBy(() -> client.send(null))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void sendRejectsBlankToUser() {
        SubscribeMessageSendRequest request = new SubscribeMessageSendRequest();
        request.setTemplateId("template-1");

        assertThatThrownBy(() -> client.send(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("toUser");
    }

    @Test
    void sendRejectsBlankTemplateId() {
        SubscribeMessageSendRequest request = new SubscribeMessageSendRequest();
        request.setToUser("openid-1");

        assertThatThrownBy(() -> client.send(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("templateId");
    }

    @Test
    void sendConvertsRequestAndUsesDefaults() throws Exception {
        SubscribeMessageSendRequest request = new SubscribeMessageSendRequest();
        request.setToUser("openid-1");
        request.setTemplateId("template-1");
        request.setPage("pages/index/index");
        request.setData(Arrays.asList(data("thing1", "hello"), data("time2", "2026-04-30")));

        SubscribeMessageSendResponse response = client.send(request);

        ArgumentCaptor<WxMaSubscribeMessage> captor = ArgumentCaptor.forClass(WxMaSubscribeMessage.class);
        verify(wxMaSubscribeService).sendSubscribeMsg(captor.capture());

        WxMaSubscribeMessage message = captor.getValue();
        assertThat(message.getToUser()).isEqualTo("openid-1");
        assertThat(message.getTemplateId()).isEqualTo("template-1");
        assertThat(message.getPage()).isEqualTo("pages/index/index");
        assertThat(message.getMiniprogramState()).isEqualTo("developer");
        assertThat(message.getLang()).isEqualTo("en_US");
        assertThat(message.getData()).hasSize(2);
        assertThat(message.getData().get(0).getName()).isEqualTo("thing1");
        assertThat(message.getData().get(0).getValue()).isEqualTo("hello");
        assertThat(message.getData().get(1).getName()).isEqualTo("time2");
        assertThat(message.getData().get(1).getValue()).isEqualTo("2026-04-30");
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getRawResponse()).isEqualTo("OK");
    }

    @Test
    void sendUsesRequestStateAndLangWhenProvided() throws Exception {
        SubscribeMessageSendRequest request = new SubscribeMessageSendRequest();
        request.setToUser("openid-1");
        request.setTemplateId("template-1");
        request.setMiniProgramState("trial");
        request.setLang("zh_CN");

        client.send(request);

        ArgumentCaptor<WxMaSubscribeMessage> captor = ArgumentCaptor.forClass(WxMaSubscribeMessage.class);
        verify(wxMaSubscribeService).sendSubscribeMsg(captor.capture());
        assertThat(captor.getValue().getMiniprogramState()).isEqualTo("trial");
        assertThat(captor.getValue().getLang()).isEqualTo("zh_CN");
    }

    @Test
    void sendConvertsSdkException() throws Exception {
        SubscribeMessageSendRequest request = new SubscribeMessageSendRequest();
        request.setToUser("openid-1");
        request.setTemplateId("template-1");
        org.mockito.Mockito.doThrow(new WxErrorException("sdk failed"))
                .when(wxMaSubscribeService).sendSubscribeMsg(any(WxMaSubscribeMessage.class));

        assertThatThrownBy(() -> client.send(request))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("subscribe message send failed");
    }

    private SubscribeMessageData data(String name, String value) {
        SubscribeMessageData data = new SubscribeMessageData();
        data.setName(name);
        data.setValue(value);
        return data;
    }
}
