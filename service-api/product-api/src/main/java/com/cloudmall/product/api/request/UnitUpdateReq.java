package com.cloudmall.product.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.mybatisplus.enums.StatusEnum;

/**
 * 单位更新请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitUpdateReq {

    private String name;
    private Integer sort;
    private StatusEnum status;
}
