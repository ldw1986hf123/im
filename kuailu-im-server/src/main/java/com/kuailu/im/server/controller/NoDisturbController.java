package com.kuailu.im.server.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.AppException;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.NoDisturb;
import com.kuailu.im.server.service.IConversationService;
import com.kuailu.im.server.service.INoDisturbService;
import com.kuailu.im.server.vo.NoDisturbVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author liangdl
 * @since 2023-05-17
 */
@Slf4j
@RestController
@RequestMapping(value = "/noDisturb")
public class NoDisturbController {

    @Autowired
    INoDisturbService noDisturbService;


    @Autowired
    IConversationService conversationService;

    @PostMapping("/add")
    public ResponseModel add(@RequestBody NoDisturbVo noDisturbVo) {
        log.info("no disturb add 参数.  {}", JSONUtil.toJsonStr(noDisturbVo));
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        try {
            NoDisturb noDisturb = BeanUtil.copyProperties(noDisturbVo, NoDisturb.class);
            noDisturb.setCreatedTime(new Date());
            noDisturb.setCreatedBy(noDisturb.getUserId());
            noDisturb.setUpdatedTime(new Date());
            noDisturb.setUpdatedBy(noDisturb.getUserId());
            UpdateWrapper updateWrapper = new UpdateWrapper<NoDisturb>()
                    .eq("user_id", noDisturbVo.getUserId())
                    .eq("conversation_id", noDisturbVo.getConversationId());
            noDisturbService.saveOrUpdate(noDisturb, updateWrapper);
        } catch (Exception e) {
            log.error("no disturb add exception.noDisturbVo：{}", JSONUtil.toJsonStr(noDisturbVo), e);
        }
        conversationService.updateConversationNoDisturb(noDisturbVo.getUserId(), noDisturbVo.getConversationId(), YesOrNoEnum.YES);
        return model;
    }

    @PostMapping("/delete")
    public ResponseModel delete(@RequestBody NoDisturbVo noDisturbVo) {
        log.info("no disturb delete 参数.  {}", JSONUtil.toJsonStr(noDisturbVo));
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        try {
            noDisturbService.remove(new LambdaQueryWrapper<NoDisturb>().eq(NoDisturb::getUserId, noDisturbVo.getUserId()).eq(NoDisturb::getConversationId, noDisturbVo.getConversationId()));
        } catch (AppException appException) {
            model.setCode(String.valueOf(appException.getCode()));
            log.error("no disturb delete exception.noDisturbVo：{}", JSONUtil.toJsonStr(noDisturbVo));
        }
        conversationService.updateConversationNoDisturb(noDisturbVo.getUserId(), noDisturbVo.getConversationId(), YesOrNoEnum.NO);
        return model;
    }

}
