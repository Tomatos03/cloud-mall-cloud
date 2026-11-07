package com.cloudmall.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.seckill.model.entity.AuditRecordDO;
import com.cloudmall.seckill.enums.AuditStatusEnum;

/**
 * 通用审核单 Mapper
 *
 * @author : Tomatos
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecordDO> {

    /**
     * 查询指定业务对象待审核的审核单。
     *
     * @param bizType 业务类型
     * @param bizId 业务对象 ID
     * @return 待审核单，不存在时返回 null
     */
    default AuditRecordDO selectPendingByBiz(String bizType, Long bizId) {
        return selectOne(
                Wrappers.<AuditRecordDO>lambdaQuery()
                        .eq(AuditRecordDO::getBizType, bizType)
                        .eq(AuditRecordDO::getBizId, bizId)
                        .eq(AuditRecordDO::getStatus, AuditStatusEnum.PENDING)
        );
    }
}
