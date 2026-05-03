package com.lixin.capability.netease.im.provider.netease;

import com.lixin.capability.netease.im.dto.CreateImAccountRequest;
import com.lixin.capability.netease.im.dto.RefreshImTokenRequest;
import com.lixin.capability.netease.im.dto.UpdateImAccountRequest;
import com.lixin.capability.netease.im.exception.NeteaseImApiException;
import com.lixin.capability.netease.im.exception.NeteaseImParseException;
import com.lixin.capability.netease.im.properties.NeteaseImCapabilityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeteaseImHttpAccountProviderTest {
    private RecordingTransport transport;
    private NeteaseImHttpAccountProvider provider;

    @BeforeEach
    void setUp() {
        NeteaseImCapabilityProperties properties = new NeteaseImCapabilityProperties();
        properties.setAppKey("app-key");
        properties.setAppSecret("app-secret");
        properties.setBaseUrl("https://api.yunxinapi.com/nimserver");
        properties.setTimeoutMillis(3000);
        transport = new RecordingTransport();
        provider = new NeteaseImHttpAccountProvider(properties, transport, new NeteaseImSignatureSupport());
    }

    @Test
    void postsCreateAccountToOfficialPathAndMapsResponse() {
        transport.body = "{\"code\":200,\"info\":{\"accid\":\"accid-1\",\"token\":\"token-1\"},\"requestId\":\"rid-1\"}";
        CreateImAccountRequest request = new CreateImAccountRequest();
        request.setAccountId("accid-1");
        request.setName("Tom");
        request.setAvatar("https://img.example.com/a.png");
        request.setExtensionJson("{\"k\":\"v\"}");

        NeteaseImAccountResult result = provider.createAccount(request);

        assertThat(transport.url).isEqualTo("https://api.yunxinapi.com/nimserver/user/create.action");
        assertThat(transport.form).containsEntry("accid", "accid-1")
                .containsEntry("name", "Tom")
                .containsEntry("icon", "https://img.example.com/a.png")
                .containsEntry("ex", "{\"k\":\"v\"}");
        assertThat(transport.headers).containsKeys("AppKey", "Nonce", "CurTime", "CheckSum");
        assertThat(result.getToken()).isEqualTo("token-1");
        assertThat(result.getRawResponse()).isEqualTo(transport.body);
    }

    @Test
    void postsUpdateProfileToOfficialPath() {
        transport.body = "{\"code\":200}";
        UpdateImAccountRequest request = new UpdateImAccountRequest();
        request.setAccountId("accid-1");
        request.setName("Tom");

        NeteaseImAccountResult result = provider.updateAccountProfile(request);

        assertThat(transport.url).isEqualTo("https://api.yunxinapi.com/nimserver/user/updateUinfo.action");
        assertThat(transport.form).containsEntry("accid", "accid-1").containsEntry("name", "Tom");
        assertThat(result.getAccountId()).isEqualTo("accid-1");
    }

    @Test
    void postsRefreshTokenToOfficialPathAndRequiresToken() {
        transport.body = "{\"code\":200,\"info\":{\"accid\":\"accid-1\",\"token\":\"new-token\"}}";
        RefreshImTokenRequest request = new RefreshImTokenRequest();
        request.setAccountId("accid-1");

        NeteaseImAccountResult result = provider.refreshToken(request);

        assertThat(transport.url).isEqualTo("https://api.yunxinapi.com/nimserver/user/refreshToken.action");
        assertThat(result.getToken()).isEqualTo("new-token");
    }

    @Test
    void convertsHttpStatusAndFailureCodeToApiException() {
        transport.statusCode = 500;
        transport.body = "{\"code\":500}";
        assertThatThrownBy(() -> provider.createAccount(validCreateRequest()))
                .isInstanceOf(NeteaseImApiException.class);

        transport.statusCode = 200;
        transport.body = "{\"code\":414,\"desc\":\"bad request\"}";
        assertThatThrownBy(() -> provider.createAccount(validCreateRequest()))
                .isInstanceOf(NeteaseImApiException.class);
    }

    @Test
    void failsWhenResponseIsNullEmptyOrMissingToken() {
        transport.nullResponse = true;
        assertThatThrownBy(() -> provider.createAccount(validCreateRequest()))
                .isInstanceOf(NeteaseImParseException.class);

        transport.nullResponse = false;
        transport.body = "";
        assertThatThrownBy(() -> provider.createAccount(validCreateRequest()))
                .isInstanceOf(NeteaseImParseException.class);

        transport.body = "{\"code\":200,\"info\":{\"accid\":\"accid-1\"}}";
        assertThatThrownBy(() -> provider.createAccount(validCreateRequest()))
                .isInstanceOf(NeteaseImParseException.class);
    }

    private CreateImAccountRequest validCreateRequest() {
        CreateImAccountRequest request = new CreateImAccountRequest();
        request.setAccountId("accid-1");
        return request;
    }

    private static class RecordingTransport implements NeteaseImHttpTransport {
        private int statusCode = 200;
        private String body = "{\"code\":200,\"info\":{\"accid\":\"accid-1\",\"token\":\"token-1\"}}";
        private boolean nullResponse;
        private String url;
        private Map<String, String> headers;
        private Map<String, String> form;

        @Override
        public NeteaseImHttpResponse postForm(String url, Map<String, String> headers, Map<String, String> form, int timeoutMillis) {
            this.url = url;
            this.headers = headers;
            this.form = form;
            if (nullResponse) {
                return null;
            }
            return new NeteaseImHttpResponse(statusCode, body, Collections.emptyMap());
        }
    }
}
