package com.cloudmall.product.api.model.resp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规格组响应对象
 * <p>
 * 封装规格分组及其下的所有规格值，如颜色、尺寸等分组。
 *
 * @author : Tomatos
 * @date : 2026/9/8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecGroupResp {

    /** 规格组 ID */
    private Long groupId;

    /** 规格组名称（如颜色、尺寸） */
    private String name;

    /** 规格值列表 */
    private List<SpecValueResp> values;
}
