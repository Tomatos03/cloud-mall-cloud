package com.cloudmall.shared.api.constant;

/**
 * 文件事件类型常量。
 * <p>
 * 生产侧写入 Outbox 时使用，消费侧按同一组常量过滤。
 *
 * @author : Tomatos
 */
public interface FileEventConstants {

    /** 业务引用文件 */
    String BIND = "FILE_BIND";

    /** 业务释放文件 */
    String RELEASE = "FILE_RELEASE";
}
