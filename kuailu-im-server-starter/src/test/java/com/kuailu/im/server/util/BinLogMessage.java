package com.kuailu.im.server.util;

import lombok.Data;

@Data
public class BinLogMessage<T> {
    /**
     * 更新后数据
     */
    private T after;
    /**
     * 更新前数据
     */
    private T before;
    /**
     * 表名
     */
    private String table;
    /**
     * schema
     */
    private String schema;
    /**
     * 操作类型
     */
    private String opType;
    /**
     * 时间
     */
    private String when;
}
