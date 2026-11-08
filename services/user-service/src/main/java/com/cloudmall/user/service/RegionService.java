package com.cloudmall.user.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.api.model.resp.RegionResp;
import com.cloudmall.user.convert.RegionConverter;
import com.cloudmall.user.mapper.RegionMapper;
import com.cloudmall.user.model.entity.RegionDO;

/**
 * 行政区划服务。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionMapper regionMapper;
    private final RegionConverter regionConverter;

    /**
     * 查询下级区域。
     *
     * @param parentCode 父级代码，可为空
     * @return 区域列表
     */
    public List<RegionResp> listChildren(String parentCode) {
        if (parentCode != null && !parentCode.isBlank()) {
            RegionDO parent = Asserts.requireNonNull(
                    regionMapper.selectById(parentCode),
                    BizErrorEnum.REGION_NOT_FOUND
            );
            Asserts.isTrue(
                    parent.getLevel() == null || parent.getLevel() < 3,
                    BizErrorEnum.REGION_LEVEL_INVALID
            );
        }
        return regionConverter.toRespList(regionMapper.listChildren(parentCode));
    }

    /**
     * 查询区域路径（省 -> 市 -> 区县）。
     *
     * @param code 区域代码
     * @return 路径列表
     */
    public List<RegionResp> getPath(String code) {
        RegionDO current = Asserts.requireNonNull(
                regionMapper.selectById(code),
                BizErrorEnum.REGION_NOT_FOUND
        );
        List<RegionResp> path = new ArrayList<>();
        while (current != null) {
            path.add(regionConverter.toResp(current));
            if (current.getParentCode() == null || current.getParentCode().isBlank()) {
                break;
            }
            current = regionMapper.selectById(current.getParentCode());
        }
        Collections.reverse(path);
        return path;
    }
}
