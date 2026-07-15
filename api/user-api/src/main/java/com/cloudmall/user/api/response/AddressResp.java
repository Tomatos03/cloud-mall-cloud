package com.cloudmall.user.api.response;

import lombok.Data;

@Data
public class AddressResp {
    private Long id;
    private Long userId;
    private String consignee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String zipCode;
    private Boolean isDefault;
}
