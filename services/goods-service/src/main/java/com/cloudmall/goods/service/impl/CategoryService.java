package com.cloudmall.goods.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.goods.api.response.CategoryResp;
import com.cloudmall.goods.entity.CategoryDO;
import com.cloudmall.goods.mapper.CategoryMapper;
import com.cloudmall.goods.service.ICategoryService;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResp> listTree() {
        List<CategoryDO> all = categoryMapper.selectList(
                Wrappers.<CategoryDO>lambdaQuery()
                        .eq(CategoryDO::getStatus, 1)
                        .orderByAsc(CategoryDO::getSortOrder)
        );
        return buildTree(all, 0L);
    }

    private List<CategoryResp> buildTree(List<CategoryDO> all, Long parentId) {
        return all.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .map(c -> {
                    CategoryResp r = toResponse(c);
                    r.setChildren(buildTree(all, c.getId()));
                    return r;
                }).collect(Collectors.toList());
    }

    @Override
    public CategoryResp getById(Long id) {
        CategoryDO cat = categoryMapper.selectById(id);
        AssertUtils.notNull(cat, BizErrorCode.DATA_NOT_FOUND);
        return toResponse(cat);
    }

    private CategoryResp toResponse(CategoryDO cat) {
        return CategoryResp.builder()
                .id(cat.getId())
                .name(cat.getName())
                .parentId(cat.getParentId())
                .sortOrder(cat.getSortOrder())
                .build();
    }
}
