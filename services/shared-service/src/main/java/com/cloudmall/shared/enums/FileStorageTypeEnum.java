package com.cloudmall.shared.enums;

/**
 * 文件存储实现类型。
 *
 * @author : Tomatos
 */
public enum FileStorageTypeEnum {
    /** 本地磁盘（仅用于开发与回滚） */
    LOCAL,

    /** MinIO 对象存储 */
    MINIO
}
