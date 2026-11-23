package com.cloudmall.shared.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResp {

    /** 文件台账 ID */
    private Long id;

    /** 文件访问路径 */
    private String url;
}
