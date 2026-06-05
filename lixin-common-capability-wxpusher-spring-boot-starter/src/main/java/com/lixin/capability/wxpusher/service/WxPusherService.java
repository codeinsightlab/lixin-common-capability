package com.lixin.capability.wxpusher.service;

import com.lixin.capability.wxpusher.dto.WxPusherSendResult;

import java.util.List;

public interface WxPusherService {
    WxPusherSendResult sendToUid(String uid, String title, String content, Integer contentType, String url);
    WxPusherSendResult sendToUids(List<String> uids, String title, String content, Integer contentType, String url);
    WxPusherSendResult sendToTopic(Integer topicId, String title, String content, Integer contentType, String url);
}
