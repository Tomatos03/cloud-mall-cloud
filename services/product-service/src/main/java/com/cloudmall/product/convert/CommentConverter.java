package com.cloudmall.product.convert;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cloudmall.product.api.model.req.CommentCreateReq;
import com.cloudmall.product.api.model.resp.CommentResp;
import com.cloudmall.product.model.entity.CommentDO;

/**
 * 评论对象转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface CommentConverter {

    /**
     * CommentDO → CommentResp。
     *
     * @param comment 评论实体
     * @return 评论响应
     */
    @Mapping(target = "username", ignore = true)
    CommentResp toResp(CommentDO comment);

    /**
     * 回填评论用户昵称。
     *
     * @param comment 评论响应
     * @param username 用户昵称
     */
    @Mapping(target = "username", source = "username")
    void fillUsername(@MappingTarget CommentResp comment, String username);

    /**
     * CommentCreateReq → CommentDO。 <p>
     * userId 由服务层从当前登录用户上下文传入，审计字段由 MyBatis-Plus 自动填充， 图
     * 片列表序列化为 JSON 数组字符串。
     *
     * @param req 创建请求
     * @param userId 当前登录用户 ID
     * @return 评论实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "status", expression = "java(com.cloudmall.mybatisplus.enums.StatusEnum.ENABLED)")
    CommentDO toDO(CommentCreateReq req, Long userId);

    /**
     * 图片 URL 列表序列化为 JSON 数组字符串。
     *
     * @param images 图片 URL 列表
     * @return JSON 数组字符串，列表为空时返回 null
     */
    default String imagesToString(List<String> images) {
        return CollUtil.isEmpty(images) ? null : JSONUtil.toJsonStr(images);
    }

    /**
     * 图片 JSON 数组字符串解析为 URL 列表。
     *
     * @param images 图片 JSON 数组字符串
     * @return 图片 URL 列表，空白时返回 null
     */
    default List<String> imagesToList(String images) {
        return StrUtil.isBlank(images) ? null : JSONUtil.toList(images, String.class);
    }
}
