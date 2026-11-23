package com.cloudmall.shared.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.LimitEnum;
import com.cloudmall.shared.enums.FileStatusEnum;
import com.cloudmall.shared.model.entity.FileDO;

/**
 * 文件台账 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface FileMapper extends BaseMapper<FileDO> {

    /**
     * 查询超时未绑定的文件。
     *
     * @param before 创建时间上限
     * @param limit 单批数量
     * @return 待回收文件列表
     */
    default List<FileDO> selectExpiredPending(LocalDateTime before, int limit) {
        return selectList(
                Wrappers.<FileDO>lambdaQuery()
                        .eq(FileDO::getStatus, FileStatusEnum.PENDING)
                        .lt(FileDO::getCreateTime, before)
                        .last(LimitEnum.of(limit))
        );
    }

    /**
     * 查询超时未回收的孤儿文件。
     *
     * @param before 更新时间上限
     * @param limit 单批数量
     * @return 待回收文件列表
     */
    default List<FileDO> selectExpiredOrphan(LocalDateTime before, int limit) {
        return selectList(
                Wrappers.<FileDO>lambdaQuery()
                        .eq(FileDO::getStatus, FileStatusEnum.ORPHAN)
                        .lt(FileDO::getUpdateTime, before)
                        .last(LimitEnum.of(limit))
        );
    }

    /**
     * 按对象 key 查询台账。
     *
     * @param objectKey 对象 key
     * @return 文件台账，不存在时为空
     */
    default Optional<FileDO> selectByObjectKey(String objectKey) {
        return Optional.ofNullable(
                selectOne(Wrappers.<FileDO>lambdaQuery().eq(FileDO::getObjectKey, objectKey))
        );
    }
}
