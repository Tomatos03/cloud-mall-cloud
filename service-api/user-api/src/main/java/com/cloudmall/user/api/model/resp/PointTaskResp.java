package com.cloudmall.user.api.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 积分任务项。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTaskResp {

    private String code;

    private String name;

    private String description;

    private Integer rewardPoints;

    private String frequency;

    private Boolean completed;

    private Boolean claimed;
}
