package com.kuailu.im.server.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.NoDisturb;
import com.kuailu.im.server.model.entity.WhiteList;
import com.kuailu.im.server.model.entity.WhiteListMember;
import com.kuailu.im.server.protocol.ProtocolManager;
import com.kuailu.im.server.service.IWhiteListMemberService;
import com.kuailu.im.server.service.IWhiteListService;
import com.kuailu.im.server.vo.IsWhiteListMemberVo;
import com.kuailu.im.server.vo.WhiteListMemberVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liangdl
 * @since 2023-05-31
 */
@Slf4j
@RestController
@RequestMapping(value = "/whiteListMember")
public class WhiteListMemberController {

    @Autowired
    IWhiteListService whiteListService;


    @Autowired
    IWhiteListMemberService whiteListMemberService;

    @PostMapping("/add")
    public ResponseModel add(@RequestBody WhiteListMemberVo whiteListMemberVo) {
        log.info("whiteListMemberVo add 参数.  {}", JSONUtil.toJsonStr(whiteListMemberVo));
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        try {
            WhiteList whiteList = new WhiteList();
            whiteList.setUserId(whiteListMemberVo.getMuserId());
            whiteList.setCreatedTime(new Date());
            whiteList.setCreatedBy(whiteListMemberVo.getMuserId());
            whiteList.setUpdatedTime(new Date());
            whiteList.setUpdatedBy(whiteListMemberVo.getMuserId());
            UpdateWrapper updateWrapper0 = new UpdateWrapper<WhiteList>()
                    .eq("user_id", whiteList.getUserId());
            whiteListService.saveOrUpdate(whiteList, updateWrapper0);

            WhiteListMember whiteListMember = new WhiteListMember();
            whiteListMember.setMUserId(whiteListMemberVo.getMuserId());
            whiteListMember.setUserId(whiteListMemberVo.getUserId());
            whiteListMember.setCreatedTime(new Date());
            whiteListMember.setCreatedBy(whiteListMemberVo.getMuserId());
            whiteListMember.setUpdatedTime(new Date());
            whiteListMember.setUpdatedBy(whiteListMemberVo.getMuserId());
            UpdateWrapper updateWrapper = new UpdateWrapper<WhiteListMember>()
                    .eq("m_user_id", whiteListMember.getMUserId())
                    .eq("user_id", whiteListMember.getUserId());
            whiteListMemberService.saveOrUpdate(whiteListMember,updateWrapper);

            WhiteListMember whiteListMember1 = new WhiteListMember();
            whiteListMember1.setMUserId(whiteListMemberVo.getMuserId());
            whiteListMember1.setUserId(whiteListMemberVo.getMuserId());
            whiteListMember1.setCreatedTime(new Date());
            whiteListMember1.setCreatedBy(whiteListMemberVo.getMuserId());
            whiteListMember1.setUpdatedTime(new Date());
            whiteListMember1.setUpdatedBy(whiteListMemberVo.getMuserId());
            UpdateWrapper updateWrapper1 = new UpdateWrapper<WhiteListMember>()
                    .eq("m_user_id", whiteListMember1.getMUserId())
                    .eq("user_id", whiteListMember1.getUserId());
            whiteListMemberService.saveOrUpdate(whiteListMember1,updateWrapper1);
        } catch (Exception e) {
            log.error("whiteListMember add exception.whiteListMemberVo：{}", JSONUtil.toJsonStr(whiteListMemberVo),e);
        }
        return model;
    }

    @PostMapping("/isWhiteListMember")
    public ResponseModel isWhiteListMember(@RequestBody IsWhiteListMemberVo isWhiteListMemberVo) {
        log.info("isWhiteListMemberVo 参数.  {}", JSONUtil.toJsonStr(isWhiteListMemberVo));
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        WhiteList whiteList = whiteListService.getOne(new LambdaQueryWrapper<WhiteList>().eq(WhiteList::getUserId, isWhiteListMemberVo.getReceiverUserId()));
        if (whiteList != null) {
            List<String> userList = whiteListMemberService.list(new LambdaQueryWrapper<WhiteListMember>().eq(WhiteListMember::getMUserId, isWhiteListMemberVo.getReceiverUserId())).stream().map(WhiteListMember::getUserId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(userList)) {
                if (!userList.contains(isWhiteListMemberVo.getSenderUserId())) {
                    model.setCode(String.valueOf(ImStatus.NOT_WHITE_LIST.getCode()));
                }
            }else {
                model.setCode(String.valueOf(ImStatus.NOT_WHITE_LIST.getCode()));
            }
        }
        return model;
    }

}
