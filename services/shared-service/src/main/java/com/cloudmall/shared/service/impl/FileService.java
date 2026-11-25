package com.cloudmall.shared.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.shared.api.enums.FileBizTypeEnum;
import com.cloudmall.shared.enums.FileStatusEnum;
import com.cloudmall.shared.infrastructure.storage.IFileStorage;
import com.cloudmall.shared.mapper.FileMapper;
import com.cloudmall.shared.model.bo.FileContent;
import com.cloudmall.shared.model.bo.FileUploadPolicy;
import com.cloudmall.shared.model.bo.FileUploadResult;
import com.cloudmall.shared.model.entity.FileDO;
import com.cloudmall.shared.properties.FileProperties;
import com.cloudmall.shared.service.IFileService;

/**
 * 文件服务实现。
 * <p>
 * 上传时先写台账再落存储，保证任何一步失败都能被清理任务兜底。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService implements IFileService {

    /** 单批清理数量 */
    private static final int CLEAN_BATCH_SIZE = 200;

    /** 默认响应类型 */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /** 对象 key 的日期路径格式 */
    private static final DateTimeFormatter MONTH_PATH = DateTimeFormatter.ofPattern("yyyy/MM");

    private final FileMapper fileMapper;
    private final IFileStorage fileStorage;
    private final FileProperties fileProperties;

    @Override
    public FileUploadResult upload(
        Long ownerId,
        FileBizTypeEnum bizType,
        MultipartFile file,
        FileUploadPolicy policy
    ) {
        String extension = validateAndResolveExtension(file, policy);
        String objectKey = buildObjectKey(policy.keyPrefix(), extension);
        String url = "/" + objectKey;
        FileDO record = FileDO.builder()
                              .objectKey(objectKey)
                              .url(url)
                              .originName(file.getOriginalFilename())
                              .contentType(file.getContentType())
                              .size(file.getSize())
                              .ownerId(ownerId)
                              .bizType(bizType)
                              .status(FileStatusEnum.PENDING)
                              .build();
        fileMapper.insert(record);
        try (InputStream inputStream = file.getInputStream()) {
            fileStorage.put(objectKey, inputStream, file.getSize(), file.getContentType());
        } catch (IOException | RuntimeException e) {
            log.error("上传文件到存储失败 objectKey={}", objectKey, e);
            throw new BizException(BizErrorEnum.FILE_SAVE_FAILED);
        }
        return new FileUploadResult(record.getId(), url);
    }

    @Override
    public void bind(Long ownerId, FileBizTypeEnum bizType, Collection<String> urls) {
        if (CollUtil.isEmpty(urls)) {
            return;
        }
        fileMapper.update(
                Wrappers.<FileDO>lambdaUpdate()
                        .in(FileDO::getUrl, urls)
                        .eq(FileDO::getOwnerId, ownerId)
                        .eq(FileDO::getStatus, FileStatusEnum.PENDING)
                        .set(FileDO::getStatus, FileStatusEnum.BOUND)
                        .set(bizType != null, FileDO::getBizType, bizType)
        );
    }

    @Override
    public void release(Collection<String> urls) {
        if (CollUtil.isEmpty(urls)) {
            return;
        }
        fileMapper.update(
                Wrappers.<FileDO>lambdaUpdate()
                        .in(FileDO::getUrl, urls)
                        .eq(FileDO::getStatus, FileStatusEnum.BOUND)
                        .set(FileDO::getStatus, FileStatusEnum.ORPHAN)
        );
    }

    @Override
    public int cleanExpired() {
        LocalDateTime pendingBefore = LocalDateTime.now()
                                                   .minusSeconds(
                                                           fileProperties.getCleanup().getPendingTtlSeconds()
                                                   );
        LocalDateTime orphanBefore = LocalDateTime.now()
                                                  .minusSeconds(
                                                          fileProperties.getCleanup().getOrphanTtlSeconds()
                                                  );
        int removed = purge(FileStatusEnum.PENDING, pendingBefore);
        removed += purge(FileStatusEnum.ORPHAN, orphanBefore);
        return removed;
    }

    @Override
    public FileContent requireContent(String objectKey) {
        Asserts.notBlank(objectKey, BizErrorEnum.FILE_NOT_FOUND);
        Asserts.isTrue(objectExists(objectKey), BizErrorEnum.FILE_NOT_FOUND);
        FileDO file = fileMapper.selectByObjectKey(objectKey).orElse(null);
        if (file == null) {
            return new FileContent(DEFAULT_CONTENT_TYPE, -1L);
        }
        return new FileContent(
                StrUtil.blankToDefault(file.getContentType(), DEFAULT_CONTENT_TYPE),
                ObjectUtil.defaultIfNull(file.getSize(), -1L)
        );
    }

    @Override
    public InputStream openObject(String objectKey) {
        try {
            return fileStorage.open(objectKey);
        } catch (RuntimeException e) {
            log.error("读取文件对象失败 objectKey={}", objectKey, e);
            throw new BizException(BizErrorEnum.FILE_READ_FAILED);
        }
    }

    /**
     * 分批清理指定状态下的过期文件。
     *
     * @param status 目标状态
     * @param before 时间上限
     * @return 清理数量
     */
    private int purge(FileStatusEnum status, LocalDateTime before) {
        int removed = 0;
        while (true) {
            List<FileDO> batch = selectExpired(status, before);
            if (CollUtil.isEmpty(batch)) {
                return removed;
            }
            int deletedThisRound = 0;
            for (FileDO file : batch) {
                if (!deleteQuietly(file)) {
                    continue;
                }
                fileMapper.deleteById(file.getId());
                deletedThisRound++;
            }
            removed += deletedThisRound;
            if (deletedThisRound == 0 || batch.size() < CLEAN_BATCH_SIZE) {
                return removed;
            }
        }
    }

    /**
     * 按状态查询一批过期文件。
     *
     * @param status 目标状态
     * @param before 时间上限
     * @return 过期文件列表
     */
    private List<FileDO> selectExpired(FileStatusEnum status, LocalDateTime before) {
        return switch (status) {
            case PENDING -> fileMapper.selectExpiredPending(before, CLEAN_BATCH_SIZE);
            case ORPHAN -> fileMapper.selectExpiredOrphan(before, CLEAN_BATCH_SIZE);
            case BOUND -> List.of();
        };
    }

    /**
     * 删除存储对象，失败时保留台账等待下次重试。
     *
     * @param file 文件台账
     * @return true 表示可以继续删除台账
     */
    private boolean deleteQuietly(FileDO file) {
        try {
            fileStorage.delete(file.getObjectKey());
            return true;
        } catch (RuntimeException e) {
            log.warn("删除文件对象失败，等待下次重试 objectKey={}", file.getObjectKey(), e);
            return false;
        }
    }

    /**
     * 判断对象是否存在，存储异常时转换为读取失败。
     *
     * @param objectKey 对象 key
     * @return true 表示存在
     */
    private boolean objectExists(String objectKey) {
        try {
            return fileStorage.exists(objectKey);
        } catch (RuntimeException e) {
            log.error("查询文件对象失败 objectKey={}", objectKey, e);
            throw new BizException(BizErrorEnum.FILE_READ_FAILED);
        }
    }

    /**
     * 校验上传文件并解析扩展名。
     *
     * @param file 上传文件
     * @param policy 上传策略
     * @return 小写扩展名
     */
    private static String validateAndResolveExtension(MultipartFile file, FileUploadPolicy policy) {
        Asserts.notNull(file, BizErrorEnum.FILE_EMPTY);
        Asserts.isFalse(file.isEmpty(), BizErrorEnum.FILE_EMPTY);
        Asserts.isTrue(file.getSize() <= policy.maxSize(), BizErrorEnum.FILE_SIZE_EXCEEDS_LIMIT);
        String extension = resolveExtension(file.getOriginalFilename());
        Asserts.contains(policy.allowedExtensions(), extension, BizErrorEnum.FILE_EXTENSION_NOT_ALLOWED);
        Asserts.isTrue(
                matchesContentType(file.getContentType(), policy.allowedContentTypePrefixes()),
                BizErrorEnum.FILE_MIME_TYPE_INVALID
        );
        return extension;
    }

    /**
     * 判断 MIME 类型是否命中允许的前缀。
     *
     * @param contentType MIME 类型
     * @param prefixes 允许的前缀
     * @return true 表示命中
     */
    private static boolean matchesContentType(String contentType, Set<String> prefixes) {
        if (contentType == null) {
            return false;
        }
        return prefixes.stream().anyMatch(contentType::startsWith);
    }

    /**
     * 生成对象 key。
     *
     * @param keyPrefix 业务前缀
     * @param extension 文件扩展名
     * @return 对象 key
     */
    private static String buildObjectKey(String keyPrefix, String extension) {
        return String.format(
                "%s/%s/%s.%s",
                keyPrefix,
                LocalDateTime.now().format(MONTH_PATH),
                IdUtil.fastSimpleUUID(),
                extension
        );
    }

    /**
     * 解析小写扩展名。
     *
     * @param originalFilename 原始文件名
     * @return 小写扩展名，无法解析时为空字符串
     */
    private static String resolveExtension(String originalFilename) {
        if (StrUtil.isBlank(originalFilename)) {
            return "";
        }
        return StrUtil.subAfter(originalFilename, ".", true).toLowerCase(Locale.ROOT);
    }
}
