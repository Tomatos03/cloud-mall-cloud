package com.cloudmall.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.user.model.entity.NoticeDO;

/**
 * 首页公告 Mapper
 *
 * @author : Tomatos
 */
@Mapper
public interface NoticeMapper extends BaseMapper<NoticeDO> {

    /**
     * 查询启用中的公告，按排序值升序。
     *
     * @return 启用公告列表
     */
    default List<NoticeDO> selectEnabledOrdered() {
        return selectList(
                Wrappers.<NoticeDO>lambdaQuery()
                        .eq(NoticeDO::getStatus, StatusEnum.ENABLED)
                        .orderByAsc(NoticeDO::getSort)
        );
    }
}
