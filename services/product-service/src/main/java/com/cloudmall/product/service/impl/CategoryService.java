package com.cloudmall.product.service.impl;

import java.util.Collections;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.model.req.CategoryCreateReq;
import com.cloudmall.product.api.model.resp.CategoryNodeResp;
import com.cloudmall.product.api.model.resp.CategoryResp;
import com.cloudmall.product.convert.CategoryConverter;
import com.cloudmall.product.model.entity.CategoryDO;
import com.cloudmall.product.mapper.CategoryMapper;
import com.cloudmall.product.service.ICategoryService;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private static final int MAX_CATEGORY_LEVEL = 3;

    private final CategoryMapper categoryMapper;
    private final CategoryConverter categoryConverter;

    /**
     * 查询启用中的分类平铺列表。
     *
     * @return 数据列表
     */
    @Override
    public List<CategoryResp> listEnabled() {
        List<CategoryDO> categories = categoryMapper.selectEnabledOrdered();
        return categoryConverter.toRespList(categories);
    }

    /**
     * 查询分类树。
     *
     * @return 数据列表
     */
    @Override
    public List<CategoryNodeResp> tree() {
        List<CategoryDO> categoryList = categoryMapper.selectEnabledOrdered();

        List<TreeNode<Long>> nodeList = categoryList
                .stream()
                .map(cat -> new TreeNode<>(cat.getId(), cat.getParentId(), cat.getName(), cat.getSort()))
                .toList();

        return TreeUtil.build(nodeList, 0L)
                       .stream()
                       .map(this::treeToResp)
                       .toList();
    }

    /**
     * 获取相关数据。
     *
     * @param id 对象标识
     * @return 处理结果
     * @throws BizException 处理过程中可能抛出该异常
     */
    @Override
    public CategoryResp getById(Long id) {
        CategoryDO cat = Asserts.requireNonNull(categoryMapper.selectById(id));
        return categoryConverter.toResp(cat);
    }

    /**
     * 批量查询分类（忽略已删除）。
     *
     * @param ids 分类 ID 列表
     * @return 分类响应列表
     */
    @Override
    public List<CategoryResp> getByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<CategoryDO> categories = categoryMapper.selectBatchIds(ids);
        return categoryConverter.toRespList(categories);
    }

    /**
     * 创建并保存数据。
     *
     * @param req 请求参数
     */
    @Override
    public void create(CategoryCreateReq req) {
        CategoryDO cat = categoryConverter.toDO(req);
        int level = resolveLevel(req.getParentId());
        cat.setLevel(level);

        categoryMapper.insert(cat);
    }

    /**
     * 解析相关数据。
     *
     * @param parentId 父级标识
     * @return 结果标识或数值
     * @throws BizException 处理过程中可能抛出该异常
     */
    private int resolveLevel(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return 1;
        }
        CategoryDO parent = Asserts.requireNonNull(categoryMapper.selectById(parentId));
        int level = ObjectUtil.defaultIfNull(parent.getLevel(), 1) + 1;
        Asserts.isTrue(level <= MAX_CATEGORY_LEVEL, BizErrorEnum.CATEGORY_BEYOND_MAX_LEVEL);
        return level;
    }

    /**
     * 将分类树节点转换为响应对象。
     *
     * @param tree 资源树
     * @return 处理结果
     */
    private CategoryNodeResp treeToResp(Tree<Long> tree) {
        CategoryResp category = CategoryResp
                .builder()
                .id(tree.getId())
                .name(tree.getName().toString())
                .parentId(tree.getParentId())
                .sort(tree.getWeight() != null ? ((Number) tree.getWeight()).intValue() : null)
                .build();
        List<CategoryNodeResp> children = CollectionUtil.emptyIfNull(tree.getChildren())
                                                        .stream()
                                                        .map(this::treeToResp)
                                                        .toList();
        return CategoryNodeResp.builder()
                               .category(category)
                               .children(children)
                               .build();
    }
}
