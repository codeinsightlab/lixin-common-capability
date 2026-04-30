package com.lixin.capability.wechat.miniapp.client;

import com.lixin.capability.wechat.miniapp.dto.Code2SessionRequest;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionResponse;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberRequest;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberResponse;

public interface WechatMiniappClient {
    Code2SessionResponse code2Session(Code2SessionRequest request);
    PhoneNumberResponse getPhoneNumber(PhoneNumberRequest request);
    String getAccessToken();
}
