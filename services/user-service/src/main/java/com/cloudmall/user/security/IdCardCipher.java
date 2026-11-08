package com.cloudmall.user.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.BizException;

/**
 * 身份证号 AES-GCM 加解密工具。
 * <p>
 * 配置项 {@code cloudmall.security.id-card-key} 为口令，启动时经 SHA-256 派生 256 位密钥；
 * 密文格式为 Base64(IV || CipherText)，IV 每次随机生成。
 *
 * @author : Tomatos
 */
@Component
public class IdCardCipher {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    private final SecretKeySpec key;

    /**
     * 构造加密工具。
     *
     * @param passphrase 加密口令
     */
    public IdCardCipher(
        @Value("${cloudmall.security.id-card-key:cloudmall-dev-id-card-key}") String passphrase
    ) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                                         .digest(passphrase.getBytes(StandardCharsets.UTF_8));
            this.key = new SecretKeySpec(digest, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalStateException("初始化身份证加密密钥失败", e);
        }
    }

    /**
     * 加密身份证号。
     *
     * @param plaintext 明文身份证号
     * @return Base64 密文
     */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new BizException(BizErrorEnum.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 解密身份证号。
     *
     * @param ciphertext Base64 密文
     * @return 明文身份证号
     */
    public String decrypt(String ciphertext) {
        try {
            byte[] data = Base64.getDecoder().decode(ciphertext);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BizException(BizErrorEnum.SERVICE_UNAVAILABLE);
        }
    }
}
