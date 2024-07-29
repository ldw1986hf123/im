package com.kuailu.im.server.mq;

public enum PushMessageType {
    // 即时聊天消息
    IM,
    // 普通消息
    NORMAL,
    // 语音邀请消息
    VOIP_INVITE,
    // 语音消息数据
    VOIP_BYE,
    // 好友邀请
    FRIEND_REQUEST,
    // 语音应答消息数据
    VOIP_ANSWER,
    // 复播消息
    RECALLED,
    // 删除消息
    DELETED,
    // 私聊消息，在头部的提示栏“你有一条密聊”消息，避免将消息内容直接展示出来
    SECRET_CHAT;
}
