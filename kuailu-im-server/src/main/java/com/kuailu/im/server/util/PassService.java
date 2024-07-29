package com.kuailu.im.server.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.core.exception.PassException;
import com.kuailu.im.core.utils.HttpUtil;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.vo.UserInfoDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RefreshScope
public class PassService {
    @Value("${kuailu.apiUrl}")
    String kuailuApiUrl;

    @Value("${kuailu.appId}")
    String appId;

    /**
     * 不经过缓存，直接从pass取数据
     *
     * @param seid
     * @param userId
     * @return
     */
    public UserInfoDetail getUserDetailsByUserId(String seid, String userId) throws PassException {
        UserInfoDetail userInfoDetail = null;
        try {
            String url = kuailuApiUrl + "j" + "?" + "appid=" + appId + "&method=getUserDetailsByUserId&seid=" + seid;
            String strResp = HttpUtil.post(url, userId);

            ResponseModel responseModel = JSON.parseObject(strResp, ResponseModel.class);
            String code = responseModel.getCode();
            if (!"200".equals(code)) {
                log.error("调用pass返回失败。url:{}  userId：{}", url, userId);
            } else {
                userInfoDetail = JSON.toJavaObject((JSONObject) responseModel.getData(), UserInfoDetail.class);
                userInfoDetail.setUserId(userInfoDetail.getId());
            }
        } catch (Exception e) {
            log.error("调用pass服务失败，userId:{},seid:{}", userId, seid);
        }
        return userInfoDetail;
    }


    public Integer getMsgHelperUnReadCount(String seid) {
        Integer count = 0;
        try {
            String url = kuailuApiUrl + "j" + "?" + "appid=com.kuailu.app.notification&method=getUnReadNumber&seid=" + seid;
            String strResp = HttpUtil.doPostBody(url, "");
            ResponseModel responseModel = JSON.parseObject(strResp, ResponseModel.class);
            String code = responseModel.getCode();
            if (!"200".equals(code)) {
                log.error("调用pass返回失败。url:{}  ", url);
            } else {
                count = (Integer) responseModel.getData();
            }
        } catch (Exception e) {
            log.error("调用pass服务失败， seid:{}", seid);
        }
        return count;
    }
}
