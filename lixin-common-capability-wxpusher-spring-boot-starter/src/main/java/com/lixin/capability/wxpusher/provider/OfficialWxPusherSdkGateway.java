package com.lixin.capability.wxpusher.provider;

import com.lixin.capability.wxpusher.properties.WxPusherProperties;
import com.smjcco.wxpusher.client.sdk.WxPusher;
import com.smjcco.wxpusher.client.sdk.bean.Message;
import com.smjcco.wxpusher.client.sdk.bean.MessageResult;
import com.smjcco.wxpusher.client.sdk.bean.Result;

import java.util.List;

public class OfficialWxPusherSdkGateway implements WxPusherSdkGateway {
    private final WxPusher wxPusher;

    public OfficialWxPusherSdkGateway(WxPusherProperties properties) {
        this.wxPusher = new WxPusher(properties.getAppToken());
    }

    @Override
    public Result<List<MessageResult>> send(Message message) {
        return wxPusher.send(message);
    }
}
