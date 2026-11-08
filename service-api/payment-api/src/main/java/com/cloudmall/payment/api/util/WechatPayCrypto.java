package com.cloudmall.payment.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;

import com.cloudmall.payment.api.constant.WechatPayConstants;

/**
 * 微信支付 API v3 签名与 AES-256-GCM 报文加解密工具。
 * <p>
 * 严格对齐微信支付官方文档的签名串格式与回调报文加密格式：
 * 请求签名串为 HTTP 方法、URL、时间戳、随机串、请求体五行，
 * 应答与回调签名串为时间戳、随机串、报文三行，每行以换行符结束。
 *
 * @author : Tomatos
 */
@UtilityClass
public class WechatPayCrypto {

    /**
     * 对签名串执行 SHA256 with RSA 签名并 Base64 编码。
     *
     * @param privateKey 私钥
     * @param message 待签名串
     * @return Base64 编码的签名值
     */
    public static String sign(PrivateKey privateKey, String message) {
        try {
            Signature signature = Signature.getInstance(WechatPayConstants.RSA_SIGN_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("微信支付签名失败", e);
        }
    }

    /**
     * 使用公钥验证 SHA256 with RSA 签名。
     *
     * @param publicKey 公钥
     * @param message 待验签串
     * @param signatureValue Base64 编码的签名值
     * @return true 表示验签通过
     */
    public static boolean verify(PublicKey publicKey, String message, String signatureValue) {
        if (signatureValue == null || signatureValue.startsWith("WECHATPAY/SIGNTEST/")) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance(WechatPayConstants.RSA_SIGN_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(signatureValue));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 构造微信支付 API v3 请求签名串。
     *
     * @param method HTTP 方法
     * @param url 去掉域名后的 URL（含查询参数）
     * @param timestamp 时间戳（秒）
     * @param nonce 随机串
     * @param body 请求体（GET 为空串）
     * @return 请求签名串
     */
    public static String requestMessage(
        String method,
        String url,
        String timestamp,
        String nonce,
        String body
    ) {
        return method + "\n" + url + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
    }

    /**
     * 构造微信支付 API v3 应答/回调签名串。
     *
     * @param timestamp 时间戳（秒）
     * @param nonce 随机串
     * @param body 报文主体
     * @return 应答签名串
     */
    public static String responseMessage(String timestamp, String nonce, String body) {
        return timestamp + "\n" + nonce + "\n" + body + "\n";
    }

    /**
     * 构造微信支付 API v3 Authorization 请求头。
     *
     * @param mchid 商户号
     * @param serialNo 商户证书序列号
     * @param privateKey 商户私钥
     * @param method HTTP 方法
     * @param url 去掉域名后的 URL（含查询参数）
     * @param body 请求体
     * @return Authorization 请求头值
     */
    public static String buildAuthorization(
        String mchid,
        String serialNo,
        PrivateKey privateKey,
        String method,
        String url,
        String body
    ) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = randomNonce(16);
        String message = requestMessage(method, url, timestamp, nonce, body);
        String signature = sign(privateKey, message);
        return WechatPayConstants.AUTH_SCHEME + " mchid=\"" + mchid + "\"" + ",nonce_str=\"" + nonce + "\""
                       + ",signature=\"" + signature + "\"" + ",timestamp=\"" + timestamp + "\""
                       + ",serial_no=\"" + serialNo + "\"";
    }

    /**
     * AES-256-GCM 加密并 Base64 编码，密文末尾包含认证标签。
     *
     * @param key APIv3 密钥（32 字节）
     * @param aad 附加数据，可为空
     * @param nonce 随机串
     * @param plaintext 明文
     * @return Base64 编码的密文
     */
    public static String aesGcmEncrypt(String key, String aad, String nonce, String plaintext) {
        try {
            Cipher cipher = init(Cipher.ENCRYPT_MODE, key, aad, nonce);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("微信支付回调报文加密失败", e);
        }
    }

    /**
     * AES-256-GCM 解密 Base64 编码的密文。
     *
     * @param key APIv3 密钥（32 字节）
     * @param aad 附加数据，可为空
     * @param nonce 随机串
     * @param ciphertext Base64 编码的密文
     * @return 明文
     */
    public static String aesGcmDecrypt(String key, String aad, String nonce, String ciphertext) {
        try {
            Cipher cipher = init(Cipher.DECRYPT_MODE, key, aad, nonce);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalArgumentException("微信支付回调报文解密失败", e);
        }
    }

    /**
     * 初始化 AES-GCM 加解密器。
     *
     * @param mode 加解密模式
     * @param key APIv3 密钥
     * @param aad 附加数据
     * @param iv 随机串
     * @return 初始化后的 Cipher
     * @throws GeneralSecurityException 初始化失败
     */
    private static Cipher init(int mode, String key, String aad, String iv) throws GeneralSecurityException {
        byte[] keyBytes = key == null ? new byte[0] : key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != WechatPayConstants.API_V3_KEY_LENGTH) {
            throw new IllegalArgumentException("APIv3 密钥必须为 32 字节");
        }
        if (iv == null || iv.isBlank()) {
            throw new IllegalArgumentException("AES-GCM nonce 不能为空");
        }
        Cipher cipher = Cipher.getInstance(WechatPayConstants.AES_GCM_TRANSFORMATION);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, WechatPayConstants.AES_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(
                WechatPayConstants.GCM_TAG_LENGTH_BITS,
                iv.getBytes(StandardCharsets.UTF_8)
        );
        cipher.init(mode, keySpec, spec);
        if (aad != null) {
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
        }
        return cipher;
    }

    /**
     * 读取 PEM 私钥（PKCS#8）。
     *
     * @param resource 私钥资源
     * @return 私钥
     */
    public static PrivateKey loadPrivateKey(Resource resource) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(stripPem(readResource(resource)));
            return KeyFactory.getInstance(WechatPayConstants.RSA_ALGORITHM)
                             .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("加载微信支付私钥失败: " + resource.getDescription(), e);
        }
    }

    /**
     * 读取 PEM 公钥（X.509）。
     *
     * @param resource 公钥资源
     * @return 公钥
     */
    public static PublicKey loadPublicKey(Resource resource) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(stripPem(readResource(resource)));
            return KeyFactory.getInstance(WechatPayConstants.RSA_ALGORITHM)
                             .generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("加载微信支付公钥失败: " + resource.getDescription(), e);
        }
    }

    /**
     * 读取 PEM 文件内容。
     *
     * @param resource 资源
     * @return PEM 文本
     */
    private static String readResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取密钥文件失败: " + resource.getDescription(), e);
        }
    }

    /**
     * 去掉 PEM 头尾与空白字符。
     *
     * @param pem PEM 文本
     * @return Base64 字符串
     */
    private static String stripPem(String pem) {
        return pem.replaceAll("-----BEGIN [^-]+-----", "")
                  .replaceAll("-----END [^-]+-----", "")
                  .replaceAll("\\s", "");
    }

    /**
     * 生成指定字节长度的十六进制随机串。
     *
     * @param byteLength 字节长度
     * @return 十六进制随机串
     */
    public static String randomNonce(int byteLength) {
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
