package com.lixin.capability.wechat.miniapp.client;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionRequest;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionResponse;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberRequest;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberResponse;
import me.chanjar.weixin.common.error.WxErrorException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultWechatMiniappClientTest {
    private final WxMaService wxMaService = mock(WxMaService.class);
    private final DefaultWechatMiniappClient client = new DefaultWechatMiniappClient(wxMaService);

    @Test
    void code2SessionRejectsNullRequest() {
        assertThatThrownBy(() -> client.code2Session(null))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class);
    }

    @Test
    void code2SessionRejectsBlankCode() {
        Code2SessionRequest request = new Code2SessionRequest();
        request.setCode("   ");

        assertThatThrownBy(() -> client.code2Session(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class);
    }

    @Test
    void code2SessionCallsSdkAndMapsResponse() throws Exception {
        Code2SessionRequest request = new Code2SessionRequest();
        request.setCode("login-code");
        WxMaJscode2SessionResult sdkResult = new WxMaJscode2SessionResult();
        sdkResult.setOpenid("openid-1");
        sdkResult.setUnionid("unionid-1");
        sdkResult.setSessionKey("session-key-1");
        when(wxMaService.jsCode2SessionInfo("login-code")).thenReturn(sdkResult);

        Code2SessionResponse response = client.code2Session(request);

        assertThat(response.getOpenId()).isEqualTo("openid-1");
        assertThat(response.getUnionId()).isEqualTo("unionid-1");
        assertThat(response.getSessionKey()).isEqualTo("session-key-1");
        verify(wxMaService).jsCode2SessionInfo("login-code");
    }

    @Test
    void code2SessionConvertsSdkException() throws Exception {
        Code2SessionRequest request = new Code2SessionRequest();
        request.setCode("bad-code");
        when(wxMaService.jsCode2SessionInfo("bad-code")).thenThrow(new WxErrorException("sdk failed"));

        assertThatThrownBy(() -> client.code2Session(request))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("code2Session");
    }

    @Test
    void getPhoneNumberRejectsNullRequest() {
        assertThatThrownBy(() -> client.getPhoneNumber(null))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class);
    }

    @Test
    void getPhoneNumberRejectsBlankCode() {
        PhoneNumberRequest request = new PhoneNumberRequest();
        request.setCode("");

        assertThatThrownBy(() -> client.getPhoneNumber(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class);
    }

    @Test
    void getPhoneNumberCallsSdkAndMapsResponse() throws Exception {
        PhoneNumberRequest request = new PhoneNumberRequest();
        request.setCode("phone-code");
        WxMaUserService userService = mock(WxMaUserService.class);
        WxMaPhoneNumberInfo phoneInfo = new WxMaPhoneNumberInfo();
        phoneInfo.setPhoneNumber("13800138000");
        phoneInfo.setPurePhoneNumber("13800138000");
        phoneInfo.setCountryCode("86");
        when(wxMaService.getUserService()).thenReturn(userService);
        when(userService.getPhoneNoInfo("phone-code")).thenReturn(phoneInfo);

        PhoneNumberResponse response = client.getPhoneNumber(request);

        assertThat(response.getPhoneNumber()).isEqualTo("13800138000");
        assertThat(response.getPurePhoneNumber()).isEqualTo("13800138000");
        assertThat(response.getCountryCode()).isEqualTo("86");
        verify(userService).getPhoneNoInfo("phone-code");
    }

    @Test
    void getPhoneNumberConvertsSdkException() throws Exception {
        PhoneNumberRequest request = new PhoneNumberRequest();
        request.setCode("bad-phone-code");
        WxMaUserService userService = mock(WxMaUserService.class);
        when(wxMaService.getUserService()).thenReturn(userService);
        when(userService.getPhoneNoInfo("bad-phone-code")).thenThrow(new WxErrorException("sdk failed"));

        assertThatThrownBy(() -> client.getPhoneNumber(request))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("phone number");
    }

    @Test
    void getAccessTokenCallsSdk() throws Exception {
        when(wxMaService.getAccessToken()).thenReturn("access-token");

        assertThat(client.getAccessToken()).isEqualTo("access-token");
        verify(wxMaService).getAccessToken();
    }

    @Test
    void getAccessTokenConvertsSdkException() throws Exception {
        when(wxMaService.getAccessToken()).thenThrow(new WxErrorException("sdk failed"));

        assertThatThrownBy(client::getAccessToken)
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("access_token");
    }
}
