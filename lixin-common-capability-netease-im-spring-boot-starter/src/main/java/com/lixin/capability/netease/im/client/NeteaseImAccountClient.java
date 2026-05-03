package com.lixin.capability.netease.im.client;

import com.lixin.capability.netease.im.dto.CreateImAccountRequest;
import com.lixin.capability.netease.im.dto.CreateImAccountResponse;
import com.lixin.capability.netease.im.dto.RefreshImTokenRequest;
import com.lixin.capability.netease.im.dto.RefreshImTokenResponse;
import com.lixin.capability.netease.im.dto.UpdateImAccountRequest;
import com.lixin.capability.netease.im.dto.UpdateImAccountResponse;

public interface NeteaseImAccountClient {
    CreateImAccountResponse createAccount(CreateImAccountRequest request);

    UpdateImAccountResponse updateAccountProfile(UpdateImAccountRequest request);

    RefreshImTokenResponse refreshToken(RefreshImTokenRequest request);
}
