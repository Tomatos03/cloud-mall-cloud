package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.goods.api.response.CategoryResponse;
import com.cloudmall.goods.entity.CategoryDO;
import com.cloudmall.goods.mapper.CategoryMapper;
import com.cloudmall.goods.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> listTree() {
        List<CategoryDO> all = categoryMapper.selectList(
                new LambdaQueryWrapper<CategoryDO>()
                        .eq(CategoryDO::getStatus, 1)
                        .orderByAsc(CategoryDO::getSortOrder)
        );
        return buildTree(all, 0L);
    }

    private List<CategoryResponse> buildTree(List<CategoryDO> all, Long parentId) {
        return all.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .map(c -> {
                    CategoryResponse r = toResponse(c);
                    r.setChildren(buildTree(all, c.getId()));
                    return r;
                }).collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getById(Long id) {
        CategoryDO cat = categoryMapper.selectById(id);
        if (cat == null) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        return toResponse(cat);
    }

    private CategoryResponse toResponse(CategoryDO cat) {
        CategoryResponse r = new CategoryResponse();
        r.setId(cat.getId());
        r.setName(cat.getName());
        r.setParentId(cat.getParentId());
        r.setSortOrder(cat.getSortOrder());
        return r;
    }
}
