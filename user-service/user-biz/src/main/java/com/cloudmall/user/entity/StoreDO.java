package com.cloudmall.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cm_store")
public class StoreDO {
    private Long id;
    private Long userId;
    private String storeName;
    private String storeLogo;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
