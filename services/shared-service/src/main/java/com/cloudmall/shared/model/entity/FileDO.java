package com.cloudmall.shared.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.shared.api.enums.FileBizTypeEnum;
import com.cloudmall.shared.enums.FileStatusEnum;

/**
 * 文件台账实体。
 * <p>
 * 对应数据库表 cm_file，记录每个上传文件的归属与生命周期状态。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_file")
public class FileDO {

    /** 文件 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 对象存储 key */
    private String objectKey;

    /** 访问地址 */
    private String url;

    /** 原始文件名 */
    private String originName;

    /** MIME 类型 */
    private String contentType;

    /** 文件大小（字节） */
    private Long size;

    /** 上传用户 ID */
    private Long ownerId;

    /** 业务类型 */
    private FileBizTypeEnum bizType;

    /** 文件状态 */
    private FileStatusEnum status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;
}
