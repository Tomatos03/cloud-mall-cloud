package com.cloudmall.common.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游标分页请求
 * <p>
 * 支持两种游标模式，按场景二选一：
 * ID 游标：{@link #cursor} 为上一页最后一条记录的 ID（首次查询传 0），按 ID 升序；
 * 时间游标：{@link #cursorTime} 为上一页最后一条记录的更新时间（首次查询传 null），按更新时间升序。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorReq {

    /** 每页最大条数 */
    public static final int MAX_SIZE = 50;

    /** ID 游标：上一页最后一条记录的 ID（首次查询传 0，与 cursorTime 二选一） */
    private Long cursor = 0L;

    /** 时间游标：上一页最后一条记录的更新时间（首次查询传 null，与 cursor 二选一） */
    private LocalDateTime cursorTime;

    /** 每页条数（默认 10，最大 50） */
    private Integer size = 10;
}
