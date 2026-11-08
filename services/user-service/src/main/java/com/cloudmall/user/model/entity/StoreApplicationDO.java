package com.cloudmall.user.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.user.api.enums.StoreApplicationStatusEnum;
import com.cloudmall.user.api.enums.StoreSubjectTypeEnum;

/**
 * 商家入驻申请实体。
 * <p>
 * 对应 cm_store_application 表；一个用户仅保留一条申请，驳回后覆盖重提。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_store_application")
public class StoreApplicationDO {

    /** 申请 ID */
    private Long id;

    /** 申请用户 ID */
    private Long userId;

    /** 申请状态 */
    private StoreApplicationStatusEnum status;

    /** 主体类型 */
    private StoreSubjectTypeEnum subjectType;

    /** 真实姓名 */
    private String realName;

    /** 身份证号 */
    private String idCard;

    /** 身份证有效期开始 */
    private LocalDate idCardValidStart;

    /** 身份证有效期结束 */
    private LocalDate idCardValidEnd;

    /** 身份证人像面照片 */
    private String idCardFront;

    /** 身份证国徽面照片 */
    private String idCardBack;

    /** 营业执照统一社会信用代码 */
    private String licenseNumber;

    /** 营业执照名称 */
    private String licenseName;

    /** 成立日期 */
    private LocalDate establishmentDate;

    /** 注册地址 */
    private String registeredAddress;

    /** 营业执照照片 */
    private String licensePhoto;

    /** 店铺名称 */
    private String storeName;

    /** 经营类目 ID 列表（JSON 文本） */
    private String categories;

    /** 发货地址 */
    private String shippingAddress;

    /** 银行账户名 */
    private String bankAccountName;

    /** 银行卡号 */
    private String bankCardNumber;

    /** 开户银行 */
    private String bankName;

    /** 开户支行 */
    private String bankBranchName;

    /** 银行预留手机号 */
    private String bankMobile;

    /** 是否同意入驻协议 */
    private Boolean agreement;

    /** 审核意见/驳回原因 */
    private String auditRemark;

    /** 审核人用户 ID */
    private Long auditorId;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 店铺 ID */
    private Long shopId;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 是否删除（0=否 1=是） */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Boolean deleted;
}
