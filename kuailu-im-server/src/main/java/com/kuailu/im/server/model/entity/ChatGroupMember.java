package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.server.constant.IM_SERVER;
import com.kuailu.im.server.enums.MemberRoleType;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("im_chat_group_member")
@NoArgsConstructor
public class ChatGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;


    /**
     * 群编号
     */
    private String groupId;

    /**
     * 账号名
     */
    private String userName;
    /**
     * 用户编号
     */
    private String userNo;

    private String userId;
    /**
     * 成员角色, OWNER, ADMIN, MEMBER
     */
    private String roleType;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;


    public ChatGroupMember(FileHelperBuilder builder) {
        this.groupId = builder.groupId;
        this.userName = builder.userName;
        this.userId=builder.userId;
        this.roleType =  builder.userId.equals(IM_SERVER.USER_ID) ? MemberRoleType.MEMBER.getCode() : MemberRoleType.OWNER.getCode();
        this.createdTime = new Date();
        this.updatedTime = new Date();
        this.createdBy = builder.userId;
    }


    public static class FileHelperBuilder {
        private String groupId;
        private String userId;
        private String userName;

        public FileHelperBuilder(String userId, String groupId, String userName) {
            this.groupId = groupId;
            this.userId = userId;
            this.userName = userName;
        }

        public ChatGroupMember build() {
            return new ChatGroupMember(this);
        }
    }


    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        ConcurrentHashMap<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
