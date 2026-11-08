package com.cloudmall.user.api.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReq {
    @NotBlank
    private String consignee;
    @NotBlank
    private String phone;
    @NotBlank
    private String province;
    @NotBlank
    private String city;
    @NotBlank
    private String district;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
    @NotBlank
    private String detail;
    private String zipCode;
    private Boolean isDefault;
}
