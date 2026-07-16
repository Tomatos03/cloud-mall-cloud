package com.cloudmall.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cm_address")
public class AddressDO {
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
