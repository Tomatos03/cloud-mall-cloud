package com.cloudmall.product.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.product.api.response.CommentResp;
import com.cloudmall.product.entity.CommentDO;

@Mapper(componentModel = "spring")
public interface CommentConverter {

    @Mapping(target = "username", ignore = true)
    CommentResp toResp(CommentDO comment);
}
