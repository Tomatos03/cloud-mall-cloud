package com.cloudmall.product.service.impl;

import java.util.List;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.common.utils.AssertUtils;
import com.cloudmall.product.api.request.CategoryCreateReq;
import com.cloudmall.product.api.response.CategoryNodeResp;
import com.cloudmall.product.api.response.CategoryResp;
import com.cloudmall.product.convert.CategoryConverter;
import com.cloudmall.product.entity.CategoryDO;
import com.cloudmall.product.mapper.CategoryMapper;
import com.cloudmall.product.service.ICategoryService;
import com.cloudmall.mybatisplus.enums.StatusEnum;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryConverter categoryConverter;

    @Override
    public List<CategoryNodeResp> tree() {
        List<CategoryDO> categoryList = categoryMapper.selectList(
                Wrappers.<CategoryDO>lambdaQuery()
                        .eq(CategoryDO::getStatus, StatusEnum.ENABLED)
                        .orderByAsc(CategoryDO::getSortOrder)
        );

        List<TreeNode<Long>> nodeList = categoryList
                .stream()
                .map(cat -> new TreeNode<>(cat.getId(), cat.getParentId(), cat.getName(), cat.getSortOrder()))
                .toList();

        return TreeUtil.build(nodeList, 0L)
                       .stream()
                       .map(this::treeToResp)
                       .toList();
    }

    @Override
    public CategoryResp getById(Long id) {
        CategoryDO cat = categoryMapper.selectById(id);
        AssertUtils.notNull(cat, BizErrorCode.DATA_NOT_FOUND);
        return categoryConverter.toResp(cat);
    }

    @Override
    public Long create(CategoryCreateReq req) {
        CategoryDO cat = categoryConverter.toDO(req);
        int level = resolveLevel(req.getParentId());
        cat.setLevel(level);

        categoryMapper.insert(cat);
        return cat.getId();
    }

    private int resolveLevel(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return 1;
        }
        CategoryDO parent = categoryMapper.selectById(parentId);
        AssertUtils.notNull(parent, BizErrorCode.DATA_NOT_FOUND);
        return parent.getLevel() + 1;
    }

    private CategoryNodeResp treeToResp(Tree<Long> tree) {
        CategoryResp category = CategoryResp.builder()
                .id(tree.getId())
                .name(tree.getName().toString())
                .parentId(tree.getParentId())
                .sortOrder(tree.getWeight() != null ? ((Number) tree.getWeight()).intValue() : null)
                .build();
        List<CategoryNodeResp> children = CollectionUtil
                .emptyIfNull(tree.getChildren())
                .stream()
                .map(this::treeToResp)
                .toList();
        return CategoryNodeResp.builder()
                               .category(category)
                               .children(children)
                               .build();
    }
}
