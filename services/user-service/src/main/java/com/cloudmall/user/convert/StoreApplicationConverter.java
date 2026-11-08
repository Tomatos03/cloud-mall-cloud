package com.cloudmall.user.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.mapstruct.Mapper;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.user.api.enums.StoreApplicationStatusEnum;
import com.cloudmall.user.api.enums.StoreSubjectTypeEnum;
import com.cloudmall.user.api.model.req.StoreRegisterReq;
import com.cloudmall.user.api.model.resp.StoreApplicationResp;
import com.cloudmall.user.model.entity.StoreApplicationDO;

/**
 * 商家入驻对象转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface StoreApplicationConverter {

    /**
     * 提交请求转申请实体。
     *
     * @param req 提交请求
     * @param userId 申请用户 ID
     * @return 入驻申请实体
     */
    default StoreApplicationDO toDO(StoreRegisterReq req, Long userId) {
        return StoreApplicationDO.builder()
                                 .userId(userId)
                                 .status(StoreApplicationStatusEnum.PENDING)
                                 .subjectType(StoreSubjectTypeEnum.fromCode(req.getSubjectType()))
                                 .realName(req.getRealName())
                                 .idCard(req.getIdCard())
                                 .idCardValidStart(parseDate(req.getIdCardValidStart()))
                                 .idCardValidEnd(parseDate(req.getIdCardValidEnd()))
                                 .idCardFront(req.getIdCardFront())
                                 .idCardBack(req.getIdCardBack())
                                 .licenseNumber(req.getLicenseNumber())
                                 .licenseName(req.getLicenseName())
                                 .establishmentDate(parseDate(req.getEstablishmentDate()))
                                 .registeredAddress(req.getRegisteredAddress())
                                 .licensePhoto(req.getLicensePhoto())
                                 .storeName(req.getStoreName())
                                 .categories(JSONUtil.toJsonStr(req.getCategories()))
                                 .shippingAddress(req.getShippingAddress())
                                 .bankAccountName(req.getBankAccountName())
                                 .bankCardNumber(req.getBankCardNumber())
                                 .bankName(req.getBankName())
                                 .bankBranchName(req.getBankBranchName())
                                 .bankMobile(req.getBankMobile())
                                 .agreement(req.getAgreement())
                                 .submitTime(LocalDateTime.now().withNano(0))
                                 .build();
    }

    /**
     * 申请实体转响应。
     *
     * @param application 入驻申请实体
     * @return 入驻申请响应
     */
    default StoreApplicationResp toResp(StoreApplicationDO application) {
        if (application == null) {
            return null;
        }
        return StoreApplicationResp.builder()
                                   .id(application.getId())
                                   .status(application.getStatus())
                                   .subjectType(application.getSubjectType())
                                   .realName(application.getRealName())
                                   .idCard(application.getIdCard())
                                   .idCardValidStart(application.getIdCardValidStart())
                                   .idCardValidEnd(application.getIdCardValidEnd())
                                   .idCardFront(application.getIdCardFront())
                                   .idCardBack(application.getIdCardBack())
                                   .licenseNumber(application.getLicenseNumber())
                                   .licenseName(application.getLicenseName())
                                   .establishmentDate(application.getEstablishmentDate())
                                   .registeredAddress(application.getRegisteredAddress())
                                   .licensePhoto(application.getLicensePhoto())
                                   .storeName(application.getStoreName())
                                   .categories(parseCategories(application.getCategories()))
                                   .shippingAddress(application.getShippingAddress())
                                   .bankAccountName(application.getBankAccountName())
                                   .bankCardNumber(application.getBankCardNumber())
                                   .bankName(application.getBankName())
                                   .bankBranchName(application.getBankBranchName())
                                   .bankMobile(application.getBankMobile())
                                   .agreement(application.getAgreement())
                                   .auditRemark(application.getAuditRemark())
                                   .auditTime(application.getAuditTime())
                                   .shopId(application.getShopId())
                                   .submitTime(application.getSubmitTime())
                                   .createTime(application.getCreateTime())
                                   .updateTime(application.getUpdateTime())
                                   .build();
    }

    /**
     * 解析日期字段。
     *
     * @param value yyyy-MM-dd 文本
     * @return 日期，空文本返回 null
     */
    default LocalDate parseDate(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BizException(BizErrorEnum.STORE_APPLICATION_SUBMIT_INVALID);
        }
    }

    /**
     * 解析经营类目 JSON。
     *
     * @param value JSON 文本
     * @return 类目 ID 列表
     */
    default List<Long> parseCategories(String value) {
        if (StrUtil.isBlank(value)) {
            return List.of();
        }
        return JSONUtil.toList(value, Long.class);
    }
}
