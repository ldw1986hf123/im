package com.kuailu.im.server.controller;

import com.kuailu.im.core.utils.HttpUtil;
import com.kuailu.im.server.starter.BaseJunitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpChatGroupControllerTest extends BaseJunitTest {

    @Test
    void saveOrUpdate() {
      /*  {
            "owner": "c9f9e947-d8e8-44be-a7c8-2559d7418fff",
                "maxMembers": 0,
                "members": [
            {
                "userNo": "1005860",
                    "userName": "宗路.ZongLu",
                    "userId": "c9f9e947-d8e8-44be-a7c8-2559d7418fff"
            },
            {
                "userNo": "5b105de6-27a2-423c-bf7b-6eaa731d0e64",
                    "userName": "严加庆.YanJiaQing",
                    "userId": "5b105de6-27a2-423c-bf7b-6eaa731d0e64"
            }
    ],
            "chatType": 0
        }
*/


        String url = UAT4 + "chatgroup/saveOrUpdate";
//        String strResp = HttpUtil.post(url, userId);
    }

    @Test
    void getByGroupId() {
    }

    @Test
    void memberInviteChatGroup() {
    }

    @Test
    void memberRemoveChatGroup() {
    }
}