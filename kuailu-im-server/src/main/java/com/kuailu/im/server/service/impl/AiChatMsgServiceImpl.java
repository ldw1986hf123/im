package com.kuailu.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.enums.AIChatTypeEnum;
import com.kuailu.im.server.enums.AITopicStatusEnum;
import com.kuailu.im.server.mapper.AIChatMsgMapper;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.*;
import com.kuailu.im.server.req.AIChatReqParam;
import com.kuailu.im.server.service.IAIAnswerExtendService;
import com.kuailu.im.server.service.IAIChatMsgService;
import com.kuailu.im.server.service.IUserAccountService;
import com.kuailu.im.server.util.RedisService;
import com.kuailu.im.server.vo.AIChatHistoryVo;
import com.kuailu.im.server.vo.AIUserStatusVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@Slf4j
public class AiChatMsgServiceImpl extends ServiceImpl<AIChatMsgMapper, AIChatMsg> implements IAIChatMsgService {

    @Autowired
    IUserAccountService userAccountService;

    @Autowired
    IAIAnswerExtendService answerExtendService;

    @Autowired
    RedisService redisService;

    @Override
    public void saveQuestion(AIChatReqParam aiChatReqParam) {
        String senderName = userAccountService.getByUserId(aiChatReqParam.getSender()).getUserName();
        AIChatMsg aiChatMsg = new AIChatMsg.QuestionBuilder(aiChatReqParam.getId(), aiChatReqParam.getSender(), senderName, aiChatReqParam.getContent(), aiChatReqParam.getTopicId()).build();

        LambdaUpdateWrapper<AIChatMsg> updateWrapper = new UpdateWrapper<AIChatMsg>().lambda()
                .eq(AIChatMsg::getMessageId, aiChatReqParam.getId());

        saveOrUpdate(aiChatMsg, updateWrapper);
    }

    @Override
    public Boolean hasAnswer(String questionMsgId) {
        QueryWrapper<AIChatMsg> queryWrapper = new QueryWrapper();
        queryWrapper.lambda()
                .eq(AIChatMsg::getType, AIChatTypeEnum.ANSWER.getCode())
                .eq(AIChatMsg::getQuestionMsgId, questionMsgId);
        return (count(queryWrapper) > 0);
    }


    @Override
    public AIChatMsg saveAnswer(String messageId, String content, String questionMsgId, String topicId, String receiver) {
        AIChatMsg answer = new AIChatMsg.AnswerBuilder(messageId, content, questionMsgId, topicId, receiver).build();
        save(answer);
        return answer;
    }

    @Override
    public ResponseModel history(String userId, String messageId, String topicId, int count) {
        ResponseModel responseModel = ResponseModel.success();
        QueryWrapper<AIChatMsg> queryWrapper = new QueryWrapper();
        queryWrapper.select("message_id", "question_msg_id", "content", "topic_id", "type", "answer_status", "operation", "created_time")
                .orderByDesc("created_time").last("limit " + count);

        AIChatMsg aiChatMsg = getOne(new QueryWrapper<AIChatMsg>().lambda().eq(AIChatMsg::getMessageId, messageId));
        if (null != aiChatMsg) {
            // 使用 or 方法构建 OR 查询条件
            queryWrapper.lambda().lt(AIChatMsg::getCreatedTime, aiChatMsg.getCreatedTime());
        }

        if (StringUtils.isNoneEmpty(topicId)) {
            queryWrapper.lambda().eq(AIChatMsg::getTopicId, topicId);
        }

        queryWrapper.lambda().and(wrapper -> wrapper.eq(AIChatMsg::getSender, userId).or().eq(AIChatMsg::getReceiver, userId));

        List<AIChatMsg> chatMsgList = list(queryWrapper);
        if (CollectionUtils.isEmpty(chatMsgList)) {
            return responseModel;
        }
        List<String> questionIdList = chatMsgList.stream().filter(chatMsg -> AIChatTypeEnum.QUESTION.getCode() == chatMsg.getType()).map(AIChatMsg::getMessageId).collect(Collectors.toList());
        //todo 答案卡片，第一个元素是最老的答案，以此类推
        List<AIAnswerExtend> extendAnswerList = answerExtendService.list(new QueryWrapper<AIAnswerExtend>().lambda().orderByAsc(AIAnswerExtend::getCreatedTime).in(AIAnswerExtend::getQuestionMsgId, questionIdList));
        Map<String, List<AIAnswerExtend>> questionIdToAnswerExtend = extendAnswerList.stream().collect(Collectors.groupingBy(AIAnswerExtend::getQuestionMsgId, Collectors.mapping(Function.identity(), Collectors.toList())));


        List<Map> aiChatHistoryVoList = new ArrayList<>();
        for (AIChatMsg chatMsg : chatMsgList) {
            Map aiChatHistoryVoMap = null;
            String content=chatMsg.getContent();

            if (AIChatTypeEnum.ANSWER.getCode() == chatMsg.getType()) {
                aiChatHistoryVoMap = new HashMap();
                List<AIChatHistoryVo> answerList = new ArrayList<>();
                //是一个答案
                String questionMsgId = chatMsg.getQuestionMsgId();
                List<AIAnswerExtend> aiAnswerExtendList = questionIdToAnswerExtend.getOrDefault(questionMsgId, new ArrayList());

                if (StringUtils.isEmpty(content)) {
                    continue;
                }
                AIChatHistoryVo firstAnswer = BeanUtil.copyProperties(chatMsg, AIChatHistoryVo.class, "content");
                List<Map> contentMap = JSONUtil.toList(content, Map.class);
                firstAnswer.setContent(contentMap);
                answerList.add(firstAnswer);

                for (AIAnswerExtend answerExtend : aiAnswerExtendList) {
                    AIChatHistoryVo single = BeanUtil.copyProperties(answerExtend, AIChatHistoryVo.class, "content");
                    single.setContent(JSONUtil.toList(answerExtend.getContent(), Map.class));
                    answerList.add(single);
                }

                aiChatHistoryVoMap.put("answerList", answerList);
                aiChatHistoryVoMap.put("questionMsgId", questionMsgId);
                aiChatHistoryVoMap.put("createdTime", chatMsg.getCreatedTime().getTime());
                aiChatHistoryVoMap.put("messageId", chatMsg.getMessageId());
                aiChatHistoryVoMap.put("topicId", chatMsg.getTopicId());
                aiChatHistoryVoMap.put("content", "");
                aiChatHistoryVoMap.put("type",chatMsg.getType());
            } else {
                aiChatHistoryVoMap = BeanUtil.beanToMap(chatMsg);
                aiChatHistoryVoMap.put("createdTime", chatMsg.getCreatedTime().getTime());

            }
            aiChatHistoryVoList.add(aiChatHistoryVoMap);
        }

        //todo
        List recommendCommand = Arrays.asList("相似指令1", "相似指令2", "相似指令3");
//        aiChatHistoryVoList.get(0).setRecommendCommand(recommendCommand);
        aiChatHistoryVoList.get(0).put("recommendCommand", recommendCommand);
        int lastIndex = aiChatHistoryVoList.size() - 1;
//        aiChatHistoryVoList.get(lastIndex).setTopicClosed(isTopicClosed(userId));
        aiChatHistoryVoList.get(lastIndex).put("topicClosed", isTopicClosed(userId));
        responseModel = ResponseModel.success(aiChatHistoryVoList);
        return responseModel;

    }

    /**
     * 每个用户维护一个十分钟的接口，存在缓存里面，十分钟没有任何数据交互的话，就自动过期
     * 一共又三种转态，
     * 1 完全空白
     * 2，上个话题已关闭
     * 3 。上个话题还没有关闭
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseModel getUserStatus(String userId) {
        AIUserStatusVo aiUserStatusVo = new AIUserStatusVo();
        LambdaQueryWrapper<AIChatMsg> wrapper = new LambdaQueryWrapper<AIChatMsg>().select(AIChatMsg::getMessageId).eq(AIChatMsg::getSender, userId).or().eq(AIChatMsg::getReceiver, userId).last("limit 1");
        List<AIChatMsg> userList = list(wrapper);

        if (CollectionUtils.isEmpty(userList)) {
            aiUserStatusVo.setUserStatus(AITopicStatusEnum.TOTAL_EMPTY.getCode());
        } else if (!isTopicClosed(userId)) {
            aiUserStatusVo.setUserStatus(AITopicStatusEnum.TOPIC_NOT_CLOSED.getCode());
        } else {
            aiUserStatusVo.setUserStatus(AITopicStatusEnum.TOPIC_CLOSED.getCode());
        }

        Map<String, String> recommendCommand = new HashMap();
        recommendCommand.put("title", "考勤查询");
        recommendCommand.put("content", "部门上周的平均工时是多少");


        Map<String, String> recommendCommand2 = new HashMap();
        recommendCommand2.put("title", "流程查询");
        recommendCommand2.put("content", "今天有哪些待办流程");


        Map<String, String> recommendCommand3 = new HashMap();
        recommendCommand3.put("title", "创作");
        recommendCommand3.put("content", "帮我创作一个周末员工培训计划");


        Map<String, String> recommendCommand4 = new HashMap();
        recommendCommand4.put("title", "搜索问答");
        recommendCommand4.put("content", "看下今天的微博热搜");

        List<Map<String, String>> list = new ArrayList<>();
        list.add(recommendCommand);
        list.add(recommendCommand2);
        list.add(recommendCommand3);
        list.add(recommendCommand4);
        aiUserStatusVo.setRecommendCommand(list);
        return ResponseModel.success(aiUserStatusVo);
    }


    private Boolean isTopicClosed(String userId) {
        String lastTopicId = getLastTopicId(userId);
        String key = RedisCacheKey.AI_CHAT_TOPIC + lastTopicId;
        return !redisService.hasKey(key);
    }


    private String getLastTopicId(String userId) {
        LambdaQueryWrapper queryWrapper = new QueryWrapper<AIChatMsg>().lambda()
                .select(AIChatMsg::getTopicId)
                .last("limit 1")
                .orderByDesc(AIChatMsg::getCreatedTime)
                .eq(AIChatMsg::getSender, userId);
        AIChatMsg aiChatMsg = getOne(queryWrapper);
        return aiChatMsg == null ? "" : aiChatMsg.getTopicId();
    }


    @Override
    public ResponseModel closeTopic(String userId, String topicId) {
        String key = RedisCacheKey.AI_CHAT_TOPIC + topicId;
        redisService.delKey(key);
        return ResponseModel.success(topicId);
    }


    @Override
    public ResponseModel continueTopic(String userId) {
        String lastTopicId = getLastTopicId(userId);
        redisService.setValue(RedisCacheKey.AI_CHAT_TOPIC + lastTopicId, userId, 10, TimeUnit.MINUTES);
        return ResponseModel.success(lastTopicId);
    }


    @Override
    public ResponseModel praise(String messageId, Integer operation, String comment) {
        UpdateWrapper<AIChatMsg> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(AIChatMsg::getMessageId, messageId);
        wrapper.lambda().set(AIChatMsg::getOperation, operation);
        wrapper.lambda().set(AIChatMsg::getComment, comment);
        if (!update(wrapper)) {
            UpdateWrapper<AIAnswerExtend> extendWrapper = new UpdateWrapper<>();
            extendWrapper.lambda().eq(AIAnswerExtend::getMessageId, messageId);
            extendWrapper.lambda().set(AIAnswerExtend::getOperation, operation);
            extendWrapper.lambda().set(AIAnswerExtend::getComment, comment);
            answerExtendService.update(extendWrapper);
        }
        return ResponseModel.success(operation);
    }


    @Override
    public ResponseModel feedbackList() {
        List data = new ArrayList();
        Map map1 = new HashMap();
        Map map2 = new HashMap();
        Map map3 = new HashMap();

        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();

        list1.add("答非所问或文不对题");
        list1.add("未识别问题中的错误");
        list1.add("未理解全部内容或要求");
        list1.add("未理解上下文信息");


        list2.add("回答中存在事实错误");
        list2.add("答案有误");
        list2.add("系统报错");
        list2.add("存在逻辑问题");
        list2.add("格式错误");
        list2.add("出现乱码");
        list2.add("内容不专业或缺乏深度");
        list2.add("回答中存在重复内容");


        list3.add("出现违法信息，有害建议");
        list3.add("存在偏见或歧视内容");
        list3.add("存在价值观问题");

        map1.put("title", "您的反馈将帮助小鹭优化进步");
        map1.put("list", list1);


        map2.put("title", "回复存在错误");
        map2.put("list", list2);

        map3.put("title", "存在违法有害信息");
        map3.put("list", list3);


        data.add(map1);
        data.add(map2);
        data.add(map3);

        return ResponseModel.success(data);

    }

    @Override
    public ResponseModel commandCenter() {
        List data = new ArrayList();
        Map map1 = new HashMap();
        Map map2 = new HashMap();
        Map map3 = new HashMap();
        Map mapCreation = new HashMap();

        List<Map> listALl = new ArrayList<>();
        List<Map> listAttendance = new ArrayList<>();
        List<String> listProcess = new ArrayList<>();
        List<String> listCreation = new ArrayList<>();


        Map subMap1 = new HashMap();
        subMap1.put("subTitle", "个人工时查询");
        subMap1.put("icon", "-------------------");
        subMap1.put("subContent", "查询员工每天工作时长，以列表形式展示，时间为这个月，姓名为：XX");

        Map subMap2 = new HashMap();
        subMap2.put("subTitle", "部门平均工时统计");
        subMap2.put("icon", "-------------------");
        subMap2.put("subContent", "统计一下快鹭产品研发中心上周每个人平均工时");


        Map subMap3 = new HashMap();
        subMap3.put("subTitle", "最长工时查询");
        subMap3.put("icon", "-------------------");
        subMap3.put("subContent", "上个月快鹭开发人员中谁的工时最长");


        Map subMap4 = new HashMap();
        subMap4.put("subTitle", "请假统计");
        subMap4.put("icon", "-------------------");
        subMap4.put("subContent", "上周几个人请假了，分别请了多少天");


        Map subMap5 = new HashMap();
        subMap5.put("subTitle", "部门工时对比");
        subMap5.put("icon", "-------------------");
        subMap5.put("subContent", "上周几个人请假了，对比一下采购和销售部门的工时");

        listAttendance.add(subMap1);
        listAttendance.add(subMap2);
        listAttendance.add(subMap3);
        listAttendance.add(subMap4);
        listAttendance.add(subMap5);

        map2.put("title", "考勤");
        map2.put("list", listAttendance);

        map3.put("title", "流程");
        map3.put("list", listProcess);


        mapCreation.put("title", "创作");
        mapCreation.put("list", listCreation);


        listALl.addAll(listAttendance);

        map1.put("title", "全部");
        map1.put("list", listALl);


        data.add(map1);
        data.add(map2);
        data.add(map3);
        data.add(mapCreation);

        return ResponseModel.success(data);
    }


}
