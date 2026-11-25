package com.cloudmall.shared.infrastructure.storage;

import java.io.InputStream;

/**
 * 文件存储抽象。
 * <p>
 * 屏蔽底层存储差异，业务只面向对象 key 读写。
 *
 * @author : Tomatos
 */
public interface IFileStorage {

    /**
     * 写入对象。
     *
     * @param objectKey 对象 key
     * @param inputStream 数据流
     * @param size 数据大小（字节）
     * @param contentType MIME 类型
     */
    void put(String objectKey, InputStream inputStream, long size, String contentType);

    /**
     * 打开对象数据流，调用方负责关闭。
     *
     * @param objectKey 对象 key
     * @return 数据流
     */
    InputStream open(String objectKey);

    /**
     * 删除对象，不存在时忽略。
     *
     * @param objectKey 对象 key
     */
    void delete(String objectKey);

    /**
     * 判断对象是否存在。
     *
     * @param objectKey 对象 key
     * @return true 表示存在
     */
    boolean exists(String objectKey);
}
