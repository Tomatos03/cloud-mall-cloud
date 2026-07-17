package com.cloudmall.goods.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.goods.api.response.CommentResp;
import com.cloudmall.goods.entity.CommentDO;

@Mapper(componentModel = "spring")
public interface CommentConverter {

    @Mapping(target = "username", ignore = true)
    CommentResp toResp(CommentDO comment);
}
