package com.cloudmall.shared.api.model.req;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.shared.api.enums.FileBizTypeEnum;

/**
 * 文件绑定请求。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileBindReq {

    /** 上传用户 ID，仅能绑定自己上传的文件 */
    @NotNull(message = "用户ID不能为空")
    private Long ownerId;

    /** 业务类型 */
    @NotNull(message = "业务类型不能为空")
    private FileBizTypeEnum bizType;

    /** 文件访问地址 */
    @NotEmpty(message = "文件地址不能为空")
    private List<String> urls;
}
