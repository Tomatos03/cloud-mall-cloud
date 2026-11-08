package com.cloudmall.user.mapper;

import java.time.LocalDate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.PointTaskRecordDO;

/**
 * 用户积分任务记录 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface PointTaskRecordMapper extends BaseMapper<PointTaskRecordDO> {

    /**
     * 查询指定日期的任务记录。
     *
     * @param userId 用户 ID
     * @param taskCode 任务编码
     * @param taskDate 任务日期
     * @return 任务记录，不存在时返回 null
     */
    default PointTaskRecordDO selectByUserAndCodeAndDate(Long userId, String taskCode, LocalDate taskDate) {
        return selectOne(
                Wrappers.<PointTaskRecordDO>lambdaQuery()
                        .eq(PointTaskRecordDO::getUserId, userId)
                        .eq(PointTaskRecordDO::getTaskCode, taskCode)
                        .eq(PointTaskRecordDO::getTaskDate, taskDate)
        );
    }
}
