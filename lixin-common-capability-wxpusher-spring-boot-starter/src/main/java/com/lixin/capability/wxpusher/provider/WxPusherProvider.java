package com.lixin.capability.wxpusher.provider;

import com.lixin.capability.wxpusher.dto.WxPusherSendRequest;
import com.lixin.capability.wxpusher.dto.WxPusherResponse;

public interface WxPusherProvider {
    WxPusherResponse send(WxPusherSendRequest request);
}
