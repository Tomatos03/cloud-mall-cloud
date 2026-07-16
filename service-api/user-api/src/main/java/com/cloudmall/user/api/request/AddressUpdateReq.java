package com.cloudmall.user.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddressUpdateReq {
    private Long id;
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
    @NotBlank
    private String detail;
    private String zipCode;
    private Boolean isDefault;
}