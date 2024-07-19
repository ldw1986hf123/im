package com.kuailu.im.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.MergedMsg;
import com.kuailu.im.server.model.entity.UserAccount;
import com.kuailu.im.server.req.MessageBody;

import java.util.List;


public interface IMergedMsgService extends IService<MergedMsg> {

    void saveMergeMessage(List<String> mergedMessageIdList, String messageId, Integer msgType, String senderName, String receiver, String mergedUserName, String currentUserId);

    void saveMergeMessage(String entityId, String messageId, Integer msgType, String senderName, String receiver, String mergedUserName);

    /**
     * 构建推送合并消息数据
     *
     * @param mergedMessageIdList
     * @return
     */
    MessageBody formMergePushMessage(List<String> mergedMessageIdList, Integer chatType, String mergedUserName,String currentUserId);

    MessageBody formMergePushMessage(String mergedMessageId, String mergedUserName);

    ResponseModel getMergeEntityDetail(String messageId);

    List<String> recursionSearchMergeMessage(String messageId, List<String> mergedMessageIdList);


    MergedMsg getByMessageId(String messageId);


    MergedMsg getButtonLevelMergeEntity(String entityId);

}
