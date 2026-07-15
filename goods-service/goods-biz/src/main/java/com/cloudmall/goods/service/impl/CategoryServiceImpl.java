package com.cloudmall.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.goods.api.response.CategoryResp;
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
    public List<CategoryResp> listTree() {
        List<CategoryDO> all = categoryMapper.selectList(
                new LambdaQueryWrapper<CategoryDO>()
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
        if (cat == null) {
            throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        }
        return toResponse(cat);
    }

    private CategoryResp toResponse(CategoryDO cat) {
        CategoryResp r = new CategoryResp();
        r.setId(cat.getId());
        r.setName(cat.getName());
        r.setParentId(cat.getParentId());
        r.setSortOrder(cat.getSortOrder());
        return r;
    }
}
