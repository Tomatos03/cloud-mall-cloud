package com.cloudmall.user.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.UserTypeEnum;
import com.cloudmall.jwt.constant.JwtClaimNames;
import com.cloudmall.jwt.service.ITokenService;
import com.cloudmall.user.model.entity.AddressDO;
import com.cloudmall.user.model.entity.UserDO;
import com.cloudmall.user.mapper.AddressMapper;
import com.cloudmall.user.mapper.UserMapper;

/**
 * 批量注册应用服务
 * <p>
 * 面向压测等实验场景：批量注册新用户 → 生成登录 token → 为每个用户创建收货
 * 地址，返回每个用户的凭证（用户名、密码、token、用户 ID、地址 ID），供
 * JMeter 等工具按行分发使用。
 * <p>
 * 压测快速路径：为规避大数据量下逐条走线上链路（注册事务消息、BCrypt 校验、
 * 权限码加载）带来的 N + 1 与执行时间过长，本类直接经 Mapper 批量查询与
 * 批量插入，token 由 {@link ITokenService} 基于已知用户上下文直发，语义与
 * 线上登录产物一致（网关只解析 userId / username / userType）。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchRegisterApp {

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;
    private final ITokenService tokenService;

    /**
     * 批量注册并生成凭证。 <p>
     * 已存在的用户名跳过注册直接登录，便于重复执行 幂等续跑；每个用户保证有 一条收货地址（缺失则创建）。
     *
     * @param usernamePrefix 用户名前缀
     * @param password 统一密码
     * @param count 需要的新用户数量
     * @return 凭证列表（username、password、token、userId、
     *         addressId）
     */
    public List<BatchUserCredential> registerAndLogin(String usernamePrefix, String password, int count) {
        List<String> usernames = buildUsernames(usernamePrefix, count);
        List<UserDO> existingUsers = userMapper.selectByUsernames(usernames);
        Map<String, UserDO> existingByUsername = existingUsers.stream()
                                                              .collect(
                                                                      Collectors.toMap(
                                                                              UserDO::getUsername,
                                                                              Function.identity(),
                                                                              (a, b) -> a
                                                                      )
                                                              );

        List<UserDO> newUsers = insertMissingUsers(usernames, existingByUsername, password);
        Map<String, UserDO> allByUsername = new HashMap<>(existingByUsername);
        newUsers.forEach(u -> allByUsername.put(u.getUsername(), u));

        Map<Long, AddressDO> addressByUserId = ensureAddresses(new ArrayList<>(allByUsername.values()));

        List<BatchUserCredential> credentials = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String username = usernames.get(i);
            UserDO user = allByUsername.get(username);
            String token = createToken(user);
            Long addressId = addressByUserId.get(user.getId()).getId();
            credentials.add(new BatchUserCredential(username, password, token, user.getId(), addressId));
        }
        log.info("批量注册并生成凭证完成 数量={} 前缀={}", count, usernamePrefix);
        return credentials;
    }

    /**
     * 生成从 {@code usernamePrefix} 起始按序号递增的用户名列表。
     *
     * @param usernamePrefix 用户名前缀
     * @param count 用户数量
     * @return 用户名列表
     */
    private List<String> buildUsernames(String usernamePrefix, int count) {
        List<String> usernames = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            usernames.add(usernamePrefix + i);
        }
        return usernames;
    }

    /**
     * 批量插入不存在的用户，密码统一 BCrypt 加密，绕过注册事务消息。
     *
     * @param usernames 全部用户名
     * @param existingByUsername 已存在用户索引
     * @param password 统一密码
     * @return 本次新增的用户列表
     */
    private List<UserDO> insertMissingUsers(
        List<String> usernames,
        Map<String, UserDO> existingByUsername,
        String password
    ) {
        String encoded = passwordEncoder.encode(password);
        List<UserDO> newUsers = new ArrayList<>();
        for (int i = 0; i < usernames.size(); i++) {
            String username = usernames.get(i);
            if (existingByUsername.containsKey(username)) {
                continue;
            }
            UserDO user = UserDO.builder()
                                .username(username)
                                .password(encoded)
                                .nickname("压测用户" + i)
                                .types(UserTypeEnum.NORMAL)
                                .build();
            userMapper.insert(user);
            newUsers.add(user);
        }
        return newUsers;
    }

    /**
     * 基于已知用户信息直发 token，与登录产物等价（网关按 userId / username /
     * userType 解析）。
     *
     * @param user 用户实体
     * @return token 字符串
     */
    private String createToken(UserDO user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimNames.USER_ID, user.getId());
        claims.put(JwtClaimNames.USERNAME, user.getUsername());
        claims.put(JwtClaimNames.USER_TYPE, UserTypeEnum.NORMAL);
        return tokenService.createToken(claims);
    }

    /**
     * 为每个用户保证一条收货地址：批量查询已有地址，缺失的批量创建。
     *
     * @param users 用户列表
     * @return userId 到地址的映射（每个用户必有）
     */
    private Map<Long, AddressDO> ensureAddresses(List<UserDO> users) {
        List<Long> userIds = users.stream()
                                  .map(UserDO::getId)
                                  .toList();
        List<AddressDO> existing = addressMapper.selectByUserIds(userIds);
        Map<Long, AddressDO> addressByUserId = existing.stream()
                                                       .collect(
                                                               Collectors.toMap(
                                                                       AddressDO::getUserId,
                                                                       Function.identity(),
                                                                       (a, b) -> a
                                                               )
                                                       );

        for (UserDO user : users) {
            if (!addressByUserId.containsKey(user.getId())) {
                AddressDO address = buildAddress(user.getId(), user.getUsername());
                addressMapper.insert(address);
                addressByUserId.put(user.getId(), address);
            }
        }
        return addressByUserId;
    }

    /**
     * 构建压测收货地址。
     *
     * @param userId 用户 ID
     * @param username 用户名
     * @return 地址实体
     */
    private AddressDO buildAddress(Long userId, String username) {
        int seq = Integer.parseInt(username.substring(username.lastIndexOf('_') + 1));
        return AddressDO.builder()
                        .userId(userId)
                        .consignee("压测用户" + seq)
                        .phone("138" + String.format("%08d", 10000000 + seq))
                        .province("广东省")
                        .city("深圳市")
                        .district("南山区")
                        .detail("科技园压测地址" + seq + "号")
                        .zipCode("518000")
                        .isDefault(true)
                        .build();
    }

    /**
     * 批量注册返回的单个用户凭证
     *
     * @param username 用户名
     * @param password 密码（明文，供压测脚本登录/下单使用）
     * @param token 登录返回的 JWT
     * @param userId 用户 ID
     * @param addressId 为用户创建的收货地址 ID（压测下单必填）
     */
    public record BatchUserCredential(
        String username,
        String password,
        String token,
        Long userId,
        Long addressId
    ) {
    }
}
