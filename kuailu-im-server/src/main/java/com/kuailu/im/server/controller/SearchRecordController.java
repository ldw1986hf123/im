package com.kuailu.im.server.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.utils.HttpUtil;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liangdl
 * @since 2023-06-15
 */
@Slf4j
@RestController
@RequestMapping(value = "/searchRecord")
@RefreshScope
public class SearchRecordController {

    @Value("${kuailu.apiUrl}")
    String kuailuApiUrl;

    @Value("${kuailu.orgAppId}")
    String orgAppId;

    @Value("${kuailu.userPortalAppId}")
    String userPortalAppId;

    @Autowired
    ISearchRecordService searchRecordService;

    @Autowired
    IChatGroupService chatGroupService;

    @Autowired
    IChatGroupMemberService chatGroupMemberService;

    @Autowired
    IChatMsgService chatMsgService;

    @Autowired
    IUserAccountService userAccountService;


    @PostMapping("/search")
    public ResponseModel search(@RequestBody SearchRecordVo searchRecordVo) {
        String searchKey = searchRecordVo.getSearchKey().trim();
        if (StringUtils.isEmpty(searchKey)) {
            return null;
        }
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        List<AddressBookResp> addressBookResps = getAddressBooks(searchRecordVo.getSeid(), searchKey);
        List<ChatGroupResp> chatGroupResps = getChatGroupResps(searchRecordVo.getUserId(), searchKey);
        List<Map<String, Object>> maps = getChatRecordResps(searchRecordVo.getUserId(), searchKey);
        SearchResp searchResp = new SearchResp();
        searchResp.setAddressBookResps(addressBookResps);
        searchResp.setChatGroupResps(chatGroupResps);
        searchResp.setChatRecordResps(maps);
        model.setData(searchResp);
        log.error("88888888888" + JSONObject.toJSONString(model));
        return model;
    }

    @PostMapping("/save")
    public ResponseModel save(@RequestBody SearchRecordVo searchRecordVo) {
        String searchKey = searchRecordVo.getSearchKey().trim();
        if (StringUtils.isEmpty(searchKey)) {
            return null;
        }
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        searchRecordService.save(searchRecordVo.getUserId(), searchKey);
        return model;
    }

    @PostMapping("/clear")
    public ResponseModel clear(@RequestBody SearchRecordClearVo searchRecordClearVo) {
        ResponseModel model = new ResponseModel();
        searchRecordService.clear(searchRecordClearVo.getUserId());
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        return model;
    }

    @GetMapping("/queryAll")
    public ResponseModel queryAll(@RequestParam(value = "userId") String userId) {
        ResponseModel model = new ResponseModel();
        List<String> records = searchRecordService.getTop10Records(userId);
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        model.setData(records);
        return model;
    }

    @GetMapping("/searchAddressBook")
    public ResponseModel searchAddressBook(@RequestParam(value = "seid") String seid, @RequestParam(value = "searchKey") String searchKey) {
        ResponseModel model = new ResponseModel();
        List<AddressBookResp> addressBookResps = getAddressBooks(seid, searchKey);
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        model.setData(addressBookResps);
        return model;
    }

    @GetMapping("/searchChatGroup")
    public ResponseModel searchChatGroup(@RequestParam(value = "userId") String userId, @RequestParam(value = "searchKey") String searchKey) {
        ResponseModel model = new ResponseModel();
        List<ChatGroupResp> chatGroupResps = getChatGroupResps(userId, searchKey);
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        model.setData(chatGroupResps);
        return model;
    }

    @GetMapping("/searchChatRecord")
    public ResponseModel searchChatRecord(@RequestParam(value = "userId") String userId, @RequestParam(value = "searchKey") String searchKey) {
        ResponseModel model = new ResponseModel();
        List<Map<String, Object>> maps = getChatRecordResps(userId, searchKey);
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        model.setData(maps);
        return model;
    }

    @GetMapping("/searchChatRecordDetail")
    public ResponseModel searchChatRecordDetail(@RequestParam(value = "userId") String userId, @RequestParam(value = "searchKey") String searchKey,
                                                @RequestParam(value = "groupId") String groupId, @RequestParam(value = "chatType") String chatType) {
        ResponseModel model = new ResponseModel();
        List<Map<String, Object>> msgIds = getChatRecordDetail(userId, searchKey, groupId, chatType);
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        model.setData(msgIds);
        return model;
    }

    private List<AddressBookResp> getAddressBooks (String seid, String searchKey) {
        List<AddressBookResp> addressBookResps = new ArrayList<>();
        try {
            String url = kuailuApiUrl + "j" + "?" + "appid=" + orgAppId + "&method=getUserList&seid=" + seid;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("queryDisabled", false);
            jsonObject.put("keyword", searchKey);
            jsonObject.put("departmentId", "");
            jsonObject.put("userStatus", "");
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("pageNo", "1");
            jsonObject1.put("pageSize", "50");
            jsonObject.put("page",jsonObject1);
            String content = jsonObject.toJSONString();
            String strResp = HttpUtil.doPostBody(url, content);
            JSONObject jsonObject2 = JSONObject.parseObject(strResp);
            String code = jsonObject2.getString("code");
            if (!"200".equals(code)) {
                log.error("调用pass返回失败。url:{}  seid：{}", url, seid);
            } else {
                JSONArray array = jsonObject2.getJSONObject("data").getJSONArray("pageData");
                if (array.isEmpty()){
                    return addressBookResps;
                }
                for (int i= 0; i< array.size(); i++) {
                    JSONObject jsonObject3 = array.getJSONObject(i);
                    if ("离职".equals(jsonObject3.getString("userStatus"))) {
                        continue;
                    }
                    AddressBookResp addressBookResp = new AddressBookResp();
                    addressBookResp.setUserName(jsonObject3.getString("userName"));
                    addressBookResp.setUserId(jsonObject3.getString("id"));
                    String avatar = kuailuApiUrl + "dlfile?appid=" + userPortalAppId + "&store=_default&groupValue=" + "&subCatalog=" + addressBookResp.getUserId();
                    addressBookResp.setAvatar(avatar);
                    addressBookResp.setOwnerOrgName(jsonObject3.getString("ownerOrgName"));
                    addressBookResp.setPositionName(jsonObject3.getString("positionName"));
                    addressBookResp.setUserStatus(jsonObject3.getString("userStatus"));
                    addressBookResps.add(addressBookResp);
                }
            }
        } catch (Exception e) {
            log.error("调用pass服务失败,seid:{},searchKey:{}, e:{}", seid, searchKey, e.toString());
        }
        return addressBookResps;
    }

    private List<ChatGroupResp> getChatGroupResps (String userId, String searchKey) {
        List<ChatGroupResp> chatGroupResps = chatGroupService.getChatGroupResps(userId, searchKey);
        return chatGroupResps;
    }

    private List<Map<String, Object>> getChatRecordResps (String userId, String searchKey) {
        List<Map<String, Object>> resultMaps = chatMsgService.getChatRecordResps(userId, searchKey);
        return resultMaps;
    }

    private List<Map<String, Object>> getChatRecordDetail (String userId, String searchKey, String groupId, String chatType) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (String.valueOf(ChatType.CHAT_TYPE_PRIVATE.getNumber()).equals(chatType)) {
            result = chatMsgService.getPrivateChatRecordDetail(userId, searchKey, groupId);
        }
        if (String.valueOf(ChatType.CHAT_TYPE_PUBLIC.getNumber()).equals(chatType)) {
            result = chatMsgService.getPublicChatRecordDetail(userId, searchKey, groupId);
        }
        return result;
    }

}


