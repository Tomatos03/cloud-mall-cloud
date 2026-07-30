package com.cloudmall.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游标分页请求
 *
 * @author : Tomatos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorReq {

    /** 每页最大条数 */
    public static final int MAX_SIZE = 50;

    /** 上一页最后一条记录的 ID（首次查询传 0） */
    private Long cursor = 0L;

    /** 每页条数（默认 10，最大 50） */
    private Integer size = 10;
}
