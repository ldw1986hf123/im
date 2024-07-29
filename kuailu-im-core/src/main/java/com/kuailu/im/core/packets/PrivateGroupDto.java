package com.kuailu.im.core.packets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2023/2/16 18:04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateGroupDto    implements Serializable {
    private  String groupId;
    private  String users;
}
