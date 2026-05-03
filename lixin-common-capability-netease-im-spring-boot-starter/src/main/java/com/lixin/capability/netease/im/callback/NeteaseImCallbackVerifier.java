package com.lixin.capability.netease.im.callback;

import com.lixin.capability.netease.im.dto.VerifyImCallbackRequest;
import com.lixin.capability.netease.im.dto.VerifyImCallbackResponse;

public interface NeteaseImCallbackVerifier {
    VerifyImCallbackResponse verify(VerifyImCallbackRequest request);
}
