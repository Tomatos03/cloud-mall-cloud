package com.cloudmall.im.model.store;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * 店铺表实体
 */
@Data
@TableName("store")
@Builder
public class Store implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String no;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 店铺头像 URL
     */
    private String avatarUrl;

    /**
     * 店主用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 店铺简介/描述
     */
    private String info;

    /**
     * 店铺顶部横幅背景图 URL
     */
    private String banner;
}