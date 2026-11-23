package com.cloudmall.shared.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.shared.service.IFileService;

/**
 * 文件过期清理任务。
 * <p>
 * 清理超过保留时长仍未绑定的文件，以及被替换后长时间未被回收的孤儿文件。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupJobHandler {

    private final IFileService fileService;

    /**
     * 清理超时的未绑定文件与孤儿文件。
     */
    @XxlJob("fileTempClean")
    public void clean() {
        int removed = fileService.cleanExpired();
        if (removed > 0) {
            log.info("文件清理完成，回收 {} 个过期文件", removed);
        }
    }
}
