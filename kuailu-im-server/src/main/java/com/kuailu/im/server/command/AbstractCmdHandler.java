/**
 *
 */
package com.kuailu.im.server.command;


import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImConst;
import com.kuailu.im.server.processor.MultiProtocolCmdProcessor;
import com.kuailu.im.server.processor.SingleProtocolCmdProcessor;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.service.impl.KafkaService;
import com.kuailu.im.server.util.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Slf4j
public abstract class AbstractCmdHandler implements CmdHandler, ImConst {

    public INoDisturbService noDisturbService;
    public IConversationService conversationService;

    public IChatGroupService iChatGroupService;

    public IChatMsgService iChatMsgService;

    public IMergedMsgService mergedMsgService;

    public IChatGroupMemberService groupMemberService;
    public IUserAccountService userAccountService;
    public PassService passService;
    public RedisService redisService;
    public KafkaService kafkaService;

    public IWhiteListMemberService whiteListMemberService;

    public IWhiteListService whiteListService;
    public IAtMsgService atMsgService;
    public IAIChatMsgService aiChatMsgService;
    public IAIAnswerExtendService answerExtendService;
    public GuavaCache guavaCache;
    /**
     * 单协议业务处理器
     */
    public SSEClient sseClient;


    /**
     * 单协议业务处理器
     */
    private SingleProtocolCmdProcessor singleProcessor;
    /**
     * 多协议业务处理器
     */
    private List<MultiProtocolCmdProcessor> multiProcessors = new ArrayList<>();


    public AbstractCmdHandler() {
        conversationService = ApplicationContextHelper.get().getBean(IConversationService.class);
        iChatGroupService = ApplicationContextHelper.get().getBean(IChatGroupService.class);
        iChatMsgService = ApplicationContextHelper.get().getBean(IChatMsgService.class);
        groupMemberService = ApplicationContextHelper.get().getBean(IChatGroupMemberService.class);
        userAccountService = ApplicationContextHelper.get().getBean(IUserAccountService.class);
        passService = ApplicationContextHelper.get().getBean(PassService.class);
        mergedMsgService = ApplicationContextHelper.get().getBean(IMergedMsgService.class);
        redisService = ApplicationContextHelper.get().getBean(RedisService.class);
        kafkaService = ApplicationContextHelper.get().getBean(KafkaService.class);
        noDisturbService = ApplicationContextHelper.get().getBean(INoDisturbService.class);
        whiteListMemberService = ApplicationContextHelper.get().getBean(IWhiteListMemberService.class);
        whiteListService = ApplicationContextHelper.get().getBean(IWhiteListService.class);
        atMsgService = ApplicationContextHelper.get().getBean(IAtMsgService.class);
        aiChatMsgService=ApplicationContextHelper.get().getBean(IAIChatMsgService.class);
        answerExtendService=ApplicationContextHelper.get().getBean(IAIAnswerExtendService.class);
        guavaCache = ApplicationContextHelper.get().getBean(GuavaCache.class);
        sseClient = ApplicationContextHelper.get().getBean(SSEClient.class);
    }

    public SingleProtocolCmdProcessor getSingleProcessor() {
        return singleProcessor;
    }

    public AbstractCmdHandler setSingleProcessor(SingleProtocolCmdProcessor singleProcessor) {
        this.singleProcessor = singleProcessor;
        return this;
    }

    public <T> T getSingleProcessor(Class<T> clazz) {
        return (T) singleProcessor;
    }

    public AbstractCmdHandler addMultiProtocolProcessor(MultiProtocolCmdProcessor processor) {
        this.multiProcessors.add(processor);
        return this;
    }

    /**
     * 根据当前通道所属协议获取cmd业务处理器
     *
     * @param imChannelContext
     * @return
     */
    public <T> T getMultiProcessor(ImChannelContext imChannelContext, Class<T> clazz) {
        T multiCmdProcessor = null;
        for (MultiProtocolCmdProcessor multiProcessor : multiProcessors) {
            if (multiProcessor.isProtocol(imChannelContext)) {
                multiCmdProcessor = (T) multiProcessor;
            }
        }
        return multiCmdProcessor;
    }


}
