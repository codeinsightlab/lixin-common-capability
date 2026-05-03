package com.lixin.capability.netease.im.client;

import com.lixin.capability.netease.im.dto.CreateImAccountRequest;
import com.lixin.capability.netease.im.dto.RefreshImTokenRequest;
import com.lixin.capability.netease.im.dto.UpdateImAccountRequest;
import com.lixin.capability.netease.im.exception.NeteaseImApiException;
import com.lixin.capability.netease.im.exception.NeteaseImInvalidRequestException;
import com.lixin.capability.netease.im.exception.NeteaseImParseException;
import com.lixin.capability.netease.im.provider.netease.NeteaseImAccountProvider;
import com.lixin.capability.netease.im.provider.netease.NeteaseImAccountResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultNeteaseImAccountClientTest {
    private RecordingProvider provider;
    private DefaultNeteaseImAccountClient client;

    @BeforeEach
    void setUp() {
        provider = new RecordingProvider();
        client = new DefaultNeteaseImAccountClient(provider);
    }

    @Test
    void rejectsInvalidCreateRequests() {
        assertThatThrownBy(() -> client.createAccount(null))
                .isInstanceOf(NeteaseImInvalidRequestException.class);

        CreateImAccountRequest request = validCreateRequest();
        request.setAccountId(" ");
        assertThatThrownBy(() -> client.createAccount(request))
                .isInstanceOf(NeteaseImInvalidRequestException.class);
    }

    @Test
    void createsAccountAndMapsResponse() {
        CreateImAccountRequest request = validCreateRequest();
        request.setName("Tom");
        request.setAvatar("https://img.example.com/a.png");
        request.setExtensionJson("{\"k\":\"v\"}");

        assertThat(client.createAccount(request).getToken()).isEqualTo("token-1");
        assertThat(provider.createRequest.getAccountId()).isEqualTo("accid-1");
    }

    @Test
    void failsWhenCreateProviderReturnsNullOrTokenMissing() {
        provider.nullResult = true;
        assertThatThrownBy(() -> client.createAccount(validCreateRequest()))
                .isInstanceOf(NeteaseImParseException.class);

        provider.nullResult = false;
        provider.missingToken = true;
        assertThatThrownBy(() -> client.createAccount(validCreateRequest()))
                .isInstanceOf(NeteaseImParseException.class);
    }

    @Test
    void propagatesCreateApiException() {
        provider.apiFailure = true;

        assertThatThrownBy(() -> client.createAccount(validCreateRequest()))
                .isInstanceOf(NeteaseImApiException.class);
    }

    @Test
    void rejectsInvalidUpdateRequests() {
        assertThatThrownBy(() -> client.updateAccountProfile(null))
                .isInstanceOf(NeteaseImInvalidRequestException.class);

        UpdateImAccountRequest blankAccount = validUpdateRequest();
        blankAccount.setAccountId("");
        assertThatThrownBy(() -> client.updateAccountProfile(blankAccount))
                .isInstanceOf(NeteaseImInvalidRequestException.class);

        UpdateImAccountRequest emptyFields = validUpdateRequest();
        emptyFields.setName(null);
        emptyFields.setAvatar(" ");
        emptyFields.setExtensionJson("");
        assertThatThrownBy(() -> client.updateAccountProfile(emptyFields))
                .isInstanceOf(NeteaseImInvalidRequestException.class);
    }

    @Test
    void updatesAccountProfileAndMapsResponse() {
        assertThat(client.updateAccountProfile(validUpdateRequest()).getAccountId()).isEqualTo("accid-1");
        assertThat(provider.updateRequest.getName()).isEqualTo("Tom");
    }

    @Test
    void failsWhenUpdateProviderReturnsNullOrFailure() {
        provider.nullResult = true;
        assertThatThrownBy(() -> client.updateAccountProfile(validUpdateRequest()))
                .isInstanceOf(NeteaseImParseException.class);

        provider.nullResult = false;
        provider.apiFailure = true;
        assertThatThrownBy(() -> client.updateAccountProfile(validUpdateRequest()))
                .isInstanceOf(NeteaseImApiException.class);
    }

    @Test
    void rejectsInvalidRefreshRequests() {
        assertThatThrownBy(() -> client.refreshToken(null))
                .isInstanceOf(NeteaseImInvalidRequestException.class);

        RefreshImTokenRequest request = validRefreshRequest();
        request.setAccountId(" ");
        assertThatThrownBy(() -> client.refreshToken(request))
                .isInstanceOf(NeteaseImInvalidRequestException.class);
    }

    @Test
    void refreshesTokenAndMapsResponse() {
        assertThat(client.refreshToken(validRefreshRequest()).getToken()).isEqualTo("token-1");
        assertThat(provider.refreshRequest.getAccountId()).isEqualTo("accid-1");
    }

    @Test
    void failsWhenRefreshProviderReturnsNullOrTokenMissingOrFailure() {
        provider.nullResult = true;
        assertThatThrownBy(() -> client.refreshToken(validRefreshRequest()))
                .isInstanceOf(NeteaseImParseException.class);

        provider.nullResult = false;
        provider.missingToken = true;
        assertThatThrownBy(() -> client.refreshToken(validRefreshRequest()))
                .isInstanceOf(NeteaseImParseException.class);

        provider.missingToken = false;
        provider.apiFailure = true;
        assertThatThrownBy(() -> client.refreshToken(validRefreshRequest()))
                .isInstanceOf(NeteaseImApiException.class);
    }

    private CreateImAccountRequest validCreateRequest() {
        CreateImAccountRequest request = new CreateImAccountRequest();
        request.setAccountId("accid-1");
        return request;
    }

    private UpdateImAccountRequest validUpdateRequest() {
        UpdateImAccountRequest request = new UpdateImAccountRequest();
        request.setAccountId("accid-1");
        request.setName("Tom");
        return request;
    }

    private RefreshImTokenRequest validRefreshRequest() {
        RefreshImTokenRequest request = new RefreshImTokenRequest();
        request.setAccountId("accid-1");
        return request;
    }

    private static class RecordingProvider implements NeteaseImAccountProvider {
        private CreateImAccountRequest createRequest;
        private UpdateImAccountRequest updateRequest;
        private RefreshImTokenRequest refreshRequest;
        private boolean nullResult;
        private boolean missingToken;
        private boolean apiFailure;

        @Override
        public NeteaseImAccountResult createAccount(CreateImAccountRequest request) {
            createRequest = request;
            return result();
        }

        @Override
        public NeteaseImAccountResult updateAccountProfile(UpdateImAccountRequest request) {
            updateRequest = request;
            return result();
        }

        @Override
        public NeteaseImAccountResult refreshToken(RefreshImTokenRequest request) {
            refreshRequest = request;
            return result();
        }

        private NeteaseImAccountResult result() {
            if (apiFailure) {
                throw new NeteaseImApiException("provider failed", "414", "{\"code\":414}");
            }
            if (nullResult) {
                return null;
            }
            NeteaseImAccountResult result = new NeteaseImAccountResult();
            result.setAccountId("accid-1");
            result.setToken(missingToken ? null : "token-1");
            result.setProviderCode("200");
            result.setProviderMessage("ok");
            result.setRequestId("request-1");
            result.setRawResponse("{\"code\":200}");
            return result;
        }
    }
}
