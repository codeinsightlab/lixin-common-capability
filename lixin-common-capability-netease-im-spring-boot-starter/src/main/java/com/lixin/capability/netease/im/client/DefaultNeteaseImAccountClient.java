package com.lixin.capability.netease.im.client;

import com.lixin.capability.netease.im.dto.CreateImAccountRequest;
import com.lixin.capability.netease.im.dto.CreateImAccountResponse;
import com.lixin.capability.netease.im.dto.RefreshImTokenRequest;
import com.lixin.capability.netease.im.dto.RefreshImTokenResponse;
import com.lixin.capability.netease.im.dto.UpdateImAccountRequest;
import com.lixin.capability.netease.im.dto.UpdateImAccountResponse;
import com.lixin.capability.netease.im.exception.NeteaseImInvalidRequestException;
import com.lixin.capability.netease.im.exception.NeteaseImParseException;
import com.lixin.capability.netease.im.provider.netease.NeteaseImAccountProvider;
import com.lixin.capability.netease.im.provider.netease.NeteaseImAccountResult;

public class DefaultNeteaseImAccountClient implements NeteaseImAccountClient {
    private final NeteaseImAccountProvider provider;

    public DefaultNeteaseImAccountClient(NeteaseImAccountProvider provider) {
        this.provider = provider;
    }

    @Override
    public CreateImAccountResponse createAccount(CreateImAccountRequest request) {
        validateCreateRequest(request);
        NeteaseImAccountResult result = provider.createAccount(request);
        validateAccountResult(result, true);

        CreateImAccountResponse response = new CreateImAccountResponse();
        response.setAccountId(result.getAccountId());
        response.setToken(result.getToken());
        response.setProviderCode(result.getProviderCode());
        response.setProviderMessage(result.getProviderMessage());
        response.setRequestId(result.getRequestId());
        response.setRawResponse(result.getRawResponse());
        return response;
    }

    @Override
    public UpdateImAccountResponse updateAccountProfile(UpdateImAccountRequest request) {
        validateUpdateRequest(request);
        NeteaseImAccountResult result = provider.updateAccountProfile(request);
        validateAccountResult(result, false);

        UpdateImAccountResponse response = new UpdateImAccountResponse();
        response.setAccountId(result.getAccountId());
        response.setProviderCode(result.getProviderCode());
        response.setProviderMessage(result.getProviderMessage());
        response.setRequestId(result.getRequestId());
        response.setRawResponse(result.getRawResponse());
        return response;
    }

    @Override
    public RefreshImTokenResponse refreshToken(RefreshImTokenRequest request) {
        validateRefreshRequest(request);
        NeteaseImAccountResult result = provider.refreshToken(request);
        validateAccountResult(result, true);

        RefreshImTokenResponse response = new RefreshImTokenResponse();
        response.setAccountId(result.getAccountId());
        response.setToken(result.getToken());
        response.setProviderCode(result.getProviderCode());
        response.setProviderMessage(result.getProviderMessage());
        response.setRequestId(result.getRequestId());
        response.setRawResponse(result.getRawResponse());
        return response;
    }

    private void validateCreateRequest(CreateImAccountRequest request) {
        if (request == null) {
            throw new NeteaseImInvalidRequestException("create account request must not be null");
        }
        validateAccountId(request.getAccountId());
    }

    private void validateUpdateRequest(UpdateImAccountRequest request) {
        if (request == null) {
            throw new NeteaseImInvalidRequestException("update account request must not be null");
        }
        validateAccountId(request.getAccountId());
        if (!hasText(request.getName()) && !hasText(request.getAvatar()) && !hasText(request.getExtensionJson())) {
            throw new NeteaseImInvalidRequestException("at least one account profile field must not be empty");
        }
    }

    private void validateRefreshRequest(RefreshImTokenRequest request) {
        if (request == null) {
            throw new NeteaseImInvalidRequestException("refresh token request must not be null");
        }
        validateAccountId(request.getAccountId());
    }

    private void validateAccountId(String accountId) {
        if (!hasText(accountId)) {
            throw new NeteaseImInvalidRequestException("accountId must not be empty");
        }
    }

    private void validateAccountResult(NeteaseImAccountResult result, boolean tokenRequired) {
        if (result == null) {
            throw new NeteaseImParseException("Netease IM provider returned null result");
        }
        if (!hasText(result.getAccountId())) {
            throw new NeteaseImParseException("Netease IM provider result missing accountId");
        }
        if (tokenRequired && !hasText(result.getToken())) {
            throw new NeteaseImParseException("Netease IM provider result missing token");
        }
        if (!hasText(result.getRawResponse())) {
            throw new NeteaseImParseException("Netease IM provider result missing rawResponse");
        }
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
