package com.cloudmall.user.api.model.resp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.user.api.enums.StoreApplicationStatusEnum;
import com.cloudmall.user.api.enums.StoreSubjectTypeEnum;

/**
 * 商家入驻申请响应。
 * <p>
 * 同时用于状态查询与资料回显，前端在审核驳回后可据此恢复表单。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreApplicationResp {

    /** 申请 ID */
    private Long id;

    /** 申请状态 */
    private StoreApplicationStatusEnum status;

    /** 入驻主体类型 */
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

    /** 经营类目 ID 列表 */
    private List<Long> categories;

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

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 店铺 ID（审核通过后返回） */
    private Long shopId;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
