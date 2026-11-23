package com.cloudmall.shared.service;

import java.io.InputStream;
import java.util.Collection;

import org.springframework.web.multipart.MultipartFile;

import com.cloudmall.shared.api.enums.FileBizTypeEnum;
import com.cloudmall.shared.model.FileContent;
import com.cloudmall.shared.model.FileUploadPolicy;
import com.cloudmall.shared.model.FileUploadResult;

/**
 * 文件服务。
 * <p>
 * 统一处理上传、绑定、释放与过期清理，所有文件操作都落在 cm_file 台账上。
 *
 * @author : Tomatos
 */
public interface IFileService {

    /**
     * 上传文件。
     *
     * @param ownerId 上传用户 ID
     * @param bizType 业务类型
     * @param file 上传文件
     * @param policy 上传策略
     * @return 文件 ID 与访问地址
     */
    FileUploadResult upload(
        Long ownerId,
        FileBizTypeEnum bizType,
        MultipartFile file,
        FileUploadPolicy policy
    );

    /**
     * 将文件标记为已被业务引用。
     *
     * @param ownerId 上传用户 ID，仅能绑定自己上传的文件
     * @param bizType 业务类型
     * @param urls 文件访问地址
     */
    void bind(Long ownerId, FileBizTypeEnum bizType, Collection<String> urls);

    /**
     * 将文件标记为待回收（业务替换或删除后调用）。
     *
     * @param urls 文件访问地址
     */
    void release(Collection<String> urls);

    /**
     * 清理超时的未绑定文件与孤儿文件。
     *
     * @return 清理数量
     */
    int cleanExpired();

    /**
     * 校验对象存在并返回内容元信息。
     *
     * @param objectKey 对象 key
     * @return 内容元信息
     */
    FileContent requireContent(String objectKey);

    /**
     * 打开对象数据流，调用方负责关闭。
     *
     * @param objectKey 对象 key
     * @return 数据流
     */
    InputStream openObject(String objectKey);
}
