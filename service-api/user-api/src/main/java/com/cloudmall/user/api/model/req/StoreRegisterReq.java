package com.cloudmall.user.api.model.req;

import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商家入驻资料提交请求。
 * <p>
 * 日期字段使用 yyyy-MM-dd 字符串，前端日期选择器可直接提交；
 * 个人店仅需身份信息，个体店/企业店还需营业执照信息。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRegisterReq {

    /** 入驻主体类型：personal / individual / enterprise */
    @NotBlank
    @Pattern(regexp = "personal|individual|enterprise", message = "无效的入驻主体类型")
    private String subjectType;

    /** 真实姓名 */
    @NotBlank
    @Size(max = 50)
    private String realName;

    /** 身份证号 */
    @NotBlank
    @Pattern(
            regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$",
            message = "请输入有效的18位身份证号"
    )
    private String idCard;

    /** 身份证有效期开始（yyyy-MM-dd） */
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "身份证有效期开始日期格式错误")
    private String idCardValidStart;

    /** 身份证有效期结束（yyyy-MM-dd） */
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "身份证有效期结束日期格式错误")
    private String idCardValidEnd;

    /** 身份证人像面照片 */
    @NotBlank
    @Size(max = 500)
    private String idCardFront;

    /** 身份证国徽面照片 */
    @NotBlank
    @Size(max = 500)
    private String idCardBack;

    /** 营业执照统一社会信用代码 */
    @Size(max = 64)
    private String licenseNumber;

    /** 营业执照名称 */
    @Size(max = 200)
    private String licenseName;

    /** 成立日期（yyyy-MM-dd） */
    @Pattern(regexp = "^$|\\d{4}-\\d{2}-\\d{2}", message = "成立日期格式错误")
    private String establishmentDate;

    /** 注册地址 */
    @Size(max = 500)
    private String registeredAddress;

    /** 营业执照照片 */
    @Size(max = 500)
    private String licensePhoto;

    /** 店铺名称 */
    @NotBlank
    @Size(min = 3, max = 30)
    private String storeName;

    /** 经营类目 ID 列表 */
    @NotEmpty
    @Size(max = 20)
    private List<Long> categories;

    /** 发货地址 */
    @NotBlank
    @Size(max = 500)
    private String shippingAddress;

    /** 银行账户名 */
    @NotBlank
    @Size(max = 100)
    private String bankAccountName;

    /** 银行卡号 */
    @NotBlank
    @Pattern(regexp = "^\\d{13,19}$", message = "请输入有效的银行卡号")
    private String bankCardNumber;

    /** 开户银行 */
    @NotBlank
    @Size(max = 100)
    private String bankName;

    /** 开户支行 */
    @NotBlank
    @Size(max = 200)
    private String bankBranchName;

    /** 银行预留手机号 */
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的手机号")
    private String bankMobile;

    /** 是否同意入驻协议 */
    @NotNull
    @AssertTrue(message = "请阅读并同意入驻协议")
    private Boolean agreement;

    /**
     * 是否需要营业执照。
     *
     * @return true 表示个体/企业主体
     */
    public boolean requiresLicense() {
        return !"personal".equals(this.subjectType);
    }
}
