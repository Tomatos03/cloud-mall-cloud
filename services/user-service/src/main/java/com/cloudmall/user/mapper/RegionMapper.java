package com.cloudmall.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.user.model.entity.RegionDO;

/**
 * 行政区划 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface RegionMapper extends BaseMapper<RegionDO> {

    /**
     * 查询下级区域。
     *
     * @param parentCode 父级代码，为空时查询省级
     * @return 区域列表
     */
    default List<RegionDO> listChildren(String parentCode) {
        var query = Wrappers.<RegionDO>lambdaQuery()
                            .eq(RegionDO::getEnabled, true)
                            .orderByAsc(RegionDO::getSort)
                            .orderByAsc(RegionDO::getCode);
        if (parentCode == null || parentCode.isBlank()) {
            query.isNull(RegionDO::getParentCode);
        } else {
            query.eq(RegionDO::getParentCode, parentCode);
        }
        return selectList(query);
    }
}
