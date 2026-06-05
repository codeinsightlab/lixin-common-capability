package com.lixin.capability.wxpusher.provider;

import com.smjcco.wxpusher.client.sdk.bean.Message;
import com.smjcco.wxpusher.client.sdk.bean.MessageResult;
import com.smjcco.wxpusher.client.sdk.bean.Result;

import java.util.List;

public interface WxPusherSdkGateway {
    Result<List<MessageResult>> send(Message message);
}
