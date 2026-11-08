package com.cloudmall.user.api.model.resp;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户积分流水。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointLogResp {

    private Long id;

    private Integer changeAmount;

    private Integer balanceAfter;

    private String type;

    private String description;

    private LocalDateTime createTime;
}
