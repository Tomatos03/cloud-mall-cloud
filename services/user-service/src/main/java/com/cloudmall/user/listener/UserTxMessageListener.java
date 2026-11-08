package com.cloudmall.user.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import com.cloudmall.rocketmq.producer.TxMessageHeader;
import com.cloudmall.user.mapper.UserMapper;
import com.cloudmall.user.model.entity.UserDO;

/**
 * 用户事务消息监听器
 * <p>
 * 处理用户注册事件的本地事务执行和状态回查。
 * <ul>
 *   <li>executeLocalTransaction：用户已在 register 中落库，此处直接 COMMIT</li>
 *   <li>checkLocalTransaction：查询用户表判断事务是否成功</li>
 * </ul>
 *
 * @author : Tomatos
 */
@Slf4j
@RequiredArgsConstructor
@RocketMQTransactionListener
public class UserTxMessageListener implements RocketMQLocalTransactionListener {

    private final UserMapper userMapper;

    /**
     * 执行本地事务。 <p>
     * 用户已在 UserService.register 中落库，此处只需确认并返回 COMMIT。
     *
     * @param msg 消息
     * @param arg 事务回调参数（固定为 null，业务主键从 header 读取）
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String userIdStr = (String) msg.getHeaders().get(TxMessageHeader.EVENT_ID);
        try {
            Long userId = Long.parseLong(userIdStr);
            UserDO user = userMapper.selectById(userId);
            if (user != null) {
                log.info("用户注册事务消息本地事务执行成功 userId={}", userId);
                return RocketMQLocalTransactionState.COMMIT;
            }
            log.warn("用户注册事务消息本地事务：用户不存在 userId={}，回滚", userId);
            return RocketMQLocalTransactionState.ROLLBACK;
        } catch (Exception e) {
            log.error("用户注册事务消息本地事务执行失败 userId={}", userIdStr, e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 回查本地事务状态：查询用户表判断事务是否成功。
     *
     * @param msg 消息
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String userIdStr = (String) msg.getHeaders().get(TxMessageHeader.EVENT_ID);
        try {
            Long userId = Long.parseLong(userIdStr);
            UserDO user = userMapper.selectById(userId);
            if (user != null) {
                log.info("事务消息回查：用户存在 userId={}，提交", userId);
                return RocketMQLocalTransactionState.COMMIT;
            }
            log.info("事务消息回查：用户不存在 userId={}，回滚", userId);
            return RocketMQLocalTransactionState.ROLLBACK;
        } catch (Exception e) {
            log.error("事务消息回查失败 userId={}", userIdStr, e);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
