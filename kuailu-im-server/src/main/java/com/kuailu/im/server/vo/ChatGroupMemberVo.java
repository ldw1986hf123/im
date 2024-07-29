package com.kuailu.im.server.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2022/12/6 20:31
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper=false)
public class ChatGroupMemberVo   implements Serializable {
    private static final long serialVersionUID = 7565494726667978100L;

    @NotBlank
    @Size(max = 128)
    private String userName;

    private String userId;

    private String userNo;

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        ConcurrentHashMap<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

}
