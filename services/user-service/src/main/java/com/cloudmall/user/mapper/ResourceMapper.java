package com.cloudmall.user.mapper;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.user.entity.ResourceDO;
import com.cloudmall.user.enums.ResourceTypeEnum;

/**
 * 资源权限 Mapper
 *
 * @author : Tomatos
 * @date : 2026/7/25
 */
@Mapper
public interface ResourceMapper extends BaseMapper<ResourceDO> {

    /**
     * 查询已启用且属于指定 ID 集合的资源。
     *
     * @param resourceIds 资源 ID 集合
     * @return 已启用的资源列表
     */
    default List<ResourceDO> selectEnabledByIds(Collection<Long> resourceIds) {
        return selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .in(ResourceDO::getId, resourceIds)
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
        );
    }

    /**
     * 查询已启用的指定类型资源，按排序值升序。 <p>
     * type 为空时仅按启用状态过滤。
     *
     * @param type 资源类型（可为 null）
     * @return 已启用的资源列表
     */
    default List<ResourceDO> selectEnabledByType(String type) {
        return selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .eq(type != null, ResourceDO::getType, type)
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
                        .orderByAsc(ResourceDO::getSort)
        );
    }

    /**
     * 查询指定资源的直接子资源数量。
     *
     * @param parentId 父资源 ID
     * @return 直接子资源数量
     */
    default Long countByParentId(Long parentId) {
        return selectCount(Wrappers.<ResourceDO>lambdaQuery().eq(ResourceDO::getParentId, parentId));
    }

    /**
     * 查询全部已启用的 UI 资源（菜单/目录/布局，排除按钮），按排序值升序。
     *
     * @return 已启用的 UI 资源列表
     */
    default List<ResourceDO> selectEnabledUiResources() {
        return selectList(
                Wrappers.<ResourceDO>lambdaQuery()
                        .in(ResourceDO::getType, ResourceTypeEnum.uiTypeCodes())
                        .eq(ResourceDO::getEnabled, StatusEnum.ENABLED)
                        .orderByAsc(ResourceDO::getSort)
        );
    }
}
