package com.lixin.capability.wechat.miniapp.client;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.exception.WechatCapabilityParseException;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionRequest;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionResponse;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberRequest;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberResponse;
import com.lixin.capability.wechat.miniapp.dto.WxaCodeUnlimitRequest;
import com.lixin.capability.wechat.miniapp.dto.WxaCodeUnlimitResponse;
import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

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
    void code2SessionRejectsNullSdkResponse() throws Exception {
        Code2SessionRequest request = new Code2SessionRequest();
        request.setCode("login-code");
        when(wxMaService.jsCode2SessionInfo("login-code")).thenReturn(null);

        assertThatThrownBy(() -> client.code2Session(request))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void code2SessionRejectsBlankOpenId() throws Exception {
        Code2SessionRequest request = new Code2SessionRequest();
        request.setCode("login-code");
        WxMaJscode2SessionResult sdkResult = new WxMaJscode2SessionResult();
        sdkResult.setSessionKey("session-key-1");
        when(wxMaService.jsCode2SessionInfo("login-code")).thenReturn(sdkResult);

        assertThatThrownBy(() -> client.code2Session(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("openId");
    }

    @Test
    void code2SessionRejectsBlankSessionKey() throws Exception {
        Code2SessionRequest request = new Code2SessionRequest();
        request.setCode("login-code");
        WxMaJscode2SessionResult sdkResult = new WxMaJscode2SessionResult();
        sdkResult.setOpenid("openid-1");
        when(wxMaService.jsCode2SessionInfo("login-code")).thenReturn(sdkResult);

        assertThatThrownBy(() -> client.code2Session(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("sessionKey");
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
    void getPhoneNumberRejectsNullSdkResponse() throws Exception {
        PhoneNumberRequest request = new PhoneNumberRequest();
        request.setCode("phone-code");
        WxMaUserService userService = mock(WxMaUserService.class);
        when(wxMaService.getUserService()).thenReturn(userService);
        when(userService.getPhoneNoInfo("phone-code")).thenReturn(null);

        assertThatThrownBy(() -> client.getPhoneNumber(request))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void getPhoneNumberRejectsBlankPhoneNumber() throws Exception {
        PhoneNumberRequest request = new PhoneNumberRequest();
        request.setCode("phone-code");
        WxMaUserService userService = mock(WxMaUserService.class);
        WxMaPhoneNumberInfo phoneInfo = new WxMaPhoneNumberInfo();
        phoneInfo.setPurePhoneNumber("13800138000");
        when(wxMaService.getUserService()).thenReturn(userService);
        when(userService.getPhoneNoInfo("phone-code")).thenReturn(phoneInfo);

        assertThatThrownBy(() -> client.getPhoneNumber(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("phoneNumber");
    }

    @Test
    void getPhoneNumberRejectsBlankPurePhoneNumber() throws Exception {
        PhoneNumberRequest request = new PhoneNumberRequest();
        request.setCode("phone-code");
        WxMaUserService userService = mock(WxMaUserService.class);
        WxMaPhoneNumberInfo phoneInfo = new WxMaPhoneNumberInfo();
        phoneInfo.setPhoneNumber("13800138000");
        when(wxMaService.getUserService()).thenReturn(userService);
        when(userService.getPhoneNoInfo("phone-code")).thenReturn(phoneInfo);

        assertThatThrownBy(() -> client.getPhoneNumber(request))
                .isInstanceOf(WechatCapabilityParseException.class)
                .hasMessageContaining("purePhoneNumber");
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

    @Test
    void getAccessTokenRejectsBlankSdkResponse() throws Exception {
        when(wxMaService.getAccessToken()).thenReturn(" ");

        assertThatThrownBy(client::getAccessToken)
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("access_token");
    }

    @Test
    void createWxaCodeUnlimitRejectsBlankScene() {
        WxaCodeUnlimitRequest request = validWxaCodeRequest();
        request.setScene(" ");

        assertThatThrownBy(() -> client.createWxaCodeUnlimit(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("scene");
    }

    @Test
    void createWxaCodeUnlimitRejectsLongScene() {
        WxaCodeUnlimitRequest request = validWxaCodeRequest();
        request.setScene("123456789012345678901234567890123");

        assertThatThrownBy(() -> client.createWxaCodeUnlimit(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("scene length");
    }

    @Test
    void createWxaCodeUnlimitRejectsLeadingSlashPage() {
        WxaCodeUnlimitRequest request = validWxaCodeRequest();
        request.setPage("/pages/baby/collaboration-invite");

        assertThatThrownBy(() -> client.createWxaCodeUnlimit(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("page must not start");
    }

    @Test
    void createWxaCodeUnlimitRejectsPageWithQuery() {
        WxaCodeUnlimitRequest request = validWxaCodeRequest();
        request.setPage("pages/baby/collaboration-invite?inviteToken=abc");

        assertThatThrownBy(() -> client.createWxaCodeUnlimit(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("query");
    }

    @Test
    void createWxaCodeUnlimitRejectsInvalidEnvVersion() {
        WxaCodeUnlimitRequest request = validWxaCodeRequest();
        request.setEnvVersion("gray");

        assertThatThrownBy(() -> client.createWxaCodeUnlimit(request))
                .isInstanceOf(WechatCapabilityInvalidRequestException.class)
                .hasMessageContaining("envVersion");
    }

    @Test
    void createWxaCodeUnlimitCallsSdkAndMapsImageBytes() throws Exception {
        byte[] pngBytes = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47};
        File file = Files.createTempFile("wxa-code", ".png").toFile();
        Files.write(file.toPath(), pngBytes);
        AtomicReference<WxaCodeUnlimitRequest> sdkRequest = new AtomicReference<>();
        DefaultWechatMiniappClient codeClient = new DefaultWechatMiniappClient(wxMaService, request -> {
            sdkRequest.set(request);
            return file;
        });
        WxaCodeUnlimitRequest request = validWxaCodeRequest();
        request.setCheckPath(false);
        request.setEnvVersion("trial");
        request.setWidth(280);

        WxaCodeUnlimitResponse response = codeClient.createWxaCodeUnlimit(request);

        assertThat(sdkRequest.get().getScene()).isEqualTo("invite-token");
        assertThat(sdkRequest.get().getPage()).isEqualTo("pages/baby/collaboration-invite");
        assertThat(sdkRequest.get().getCheckPath()).isFalse();
        assertThat(sdkRequest.get().getEnvVersion()).isEqualTo("trial");
        assertThat(sdkRequest.get().getWidth()).isEqualTo(280);
        assertThat(response.getBytes()).isEqualTo(pngBytes);
        assertThat(response.getContentType()).isEqualTo("image/png");
        assertThat(response.getBase64()).isEqualTo("iVBORw==");
    }

    @Test
    void createWxaCodeUnlimitNormalizesRequestBeforeSdkCall() throws Exception {
        byte[] pngBytes = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47};
        File file = Files.createTempFile("wxa-code", ".png").toFile();
        Files.write(file.toPath(), pngBytes);
        AtomicReference<WxaCodeUnlimitRequest> sdkRequest = new AtomicReference<>();
        DefaultWechatMiniappClient codeClient = new DefaultWechatMiniappClient(wxMaService, request -> {
            sdkRequest.set(request);
            return file;
        });
        WxaCodeUnlimitRequest request = new WxaCodeUnlimitRequest();
        request.setScene(" invite-token ");
        request.setPage(" pages/baby/collaboration-invite ");

        codeClient.createWxaCodeUnlimit(request);

        assertThat(sdkRequest.get().getScene()).isEqualTo("invite-token");
        assertThat(sdkRequest.get().getPage()).isEqualTo("pages/baby/collaboration-invite");
        assertThat(sdkRequest.get().getCheckPath()).isTrue();
        assertThat(sdkRequest.get().getEnvVersion()).isEqualTo("release");
        assertThat(sdkRequest.get().getWidth()).isEqualTo(430);
    }

    @Test
    void createWxaCodeUnlimitConvertsWxErrorAndDoesNotExposeSceneToken() {
        String sceneToken = "secret-invite-token";
        DefaultWechatMiniappClient codeClient = new DefaultWechatMiniappClient(wxMaService, request -> {
            throw new WxErrorException(wxError(41030, "invalid page"));
        });
        WxaCodeUnlimitRequest request = validWxaCodeRequest();
        request.setScene(sceneToken);

        assertThatThrownBy(() -> codeClient.createWxaCodeUnlimit(request))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("createWxaCodeUnlimit")
                .hasMessageContaining("page=pages/baby/collaboration-invite")
                .hasMessageContaining("sceneLength=19")
                .hasMessageContaining("envVersion=release")
                .hasMessageContaining("checkPath=true")
                .hasMessageNotContaining(sceneToken)
                .satisfies(throwable -> {
                    WechatCapabilityApiException exception = (WechatCapabilityApiException) throwable;
                    assertThat(exception.getCode()).isEqualTo("41030");
                    assertThat(exception.getRawBody()).contains("41030").contains("invalid page");
                });
    }

    @Test
    void createWxaCodeUnlimitWrapsRuntimeSdkFailureWithContext() {
        DefaultWechatMiniappClient codeClient = new DefaultWechatMiniappClient(wxMaService, request -> {
            throw new IllegalStateException("sdk transport failed");
        });
        WxaCodeUnlimitRequest request = validWxaCodeRequest();

        assertThatThrownBy(() -> codeClient.createWxaCodeUnlimit(request))
                .isInstanceOf(WechatCapabilityApiException.class)
                .hasMessageContaining("SDK call failed")
                .hasMessageContaining("sceneLength=12");
    }

    private WxaCodeUnlimitRequest validWxaCodeRequest() {
        WxaCodeUnlimitRequest request = new WxaCodeUnlimitRequest();
        request.setScene("invite-token");
        request.setPage("pages/baby/collaboration-invite");
        request.setCheckPath(true);
        request.setEnvVersion("release");
        request.setWidth(430);
        return request;
    }

    private WxError wxError(int code, String message) {
        WxError error = new WxError();
        error.setErrorCode(code);
        error.setErrorMsg(message);
        return error;
    }
}
