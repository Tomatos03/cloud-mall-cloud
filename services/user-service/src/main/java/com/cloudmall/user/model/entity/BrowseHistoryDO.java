package com.cloudmall.user.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户商品浏览记录。
 * <p>
 * 对应数据库表 cm_user_browse_history，同一用户同一商品只保留最新一条。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_user_browse_history")
public class BrowseHistoryDO {

    @TableId
    private Long id;

    private Long userId;

    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal price;

    private LocalDateTime browseTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
