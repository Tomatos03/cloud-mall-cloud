package com.cloudmall.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.PointTaskDO;

/**
 * 积分任务 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface PointTaskMapper extends BaseMapper<PointTaskDO> {

    /**
     * 查询启用的任务，按排序值升序。
     *
     * @return 任务列表
     */
    default List<PointTaskDO> listEnabled() {
        return selectList(
                Wrappers.<PointTaskDO>lambdaQuery()
                        .eq(PointTaskDO::getEnabled, true)
                        .orderByAsc(PointTaskDO::getSort)
        );
    }

    /**
     * 按编码查询任务。
     *
     * @param code 任务编码
     * @return 任务，不存在时返回 null
     */
    default PointTaskDO selectByCode(String code) {
        return selectOne(Wrappers.<PointTaskDO>lambdaQuery().eq(PointTaskDO::getCode, code));
    }
}
