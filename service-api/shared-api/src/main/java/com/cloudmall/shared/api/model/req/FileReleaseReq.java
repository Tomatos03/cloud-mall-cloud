package com.cloudmall.shared.api.model.req;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件释放请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileReleaseReq {

    /** 文件访问地址 */
    @NotEmpty(message = "文件地址不能为空")
    private List<String> urls;
}
