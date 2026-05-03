package com.lixin.capability.netease.im.provider.netease;

import com.lixin.capability.netease.im.dto.CreateImAccountRequest;
import com.lixin.capability.netease.im.dto.RefreshImTokenRequest;
import com.lixin.capability.netease.im.dto.UpdateImAccountRequest;

public interface NeteaseImAccountProvider {
    NeteaseImAccountResult createAccount(CreateImAccountRequest request);

    NeteaseImAccountResult updateAccountProfile(UpdateImAccountRequest request);

    NeteaseImAccountResult refreshToken(RefreshImTokenRequest request);
}
