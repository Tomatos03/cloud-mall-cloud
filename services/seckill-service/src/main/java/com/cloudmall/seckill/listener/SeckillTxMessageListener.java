package com.cloudmall.seckill.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import com.cloudmall.rocketmq.producer.TxMessageHeader;
import com.cloudmall.seckill.entity.SeckillOrderDO;
import com.cloudmall.seckill.mapper.SeckillOrderMapper;

/**
 * 秒杀事务消息监听器
 * <p>
 * 处理秒杀建单事件的本地事务执行和状态回查。
 * <ul>
 *   <li>executeLocalTransaction：秒杀单已在 createWithEvent 中落库，此处直接 COMMIT</li>
 *   <li>checkLocalTransaction：查询秒杀单判断事务是否成功</li>
 * </ul>
 *
 * @author : Tomatos
 */
@Slf4j
@RequiredArgsConstructor
@RocketMQTransactionListener
public class SeckillTxMessageListener implements RocketMQLocalTransactionListener {

    private final SeckillOrderMapper orderMapper;

    /**
     * 执行本地事务。 <p>
     * 秒杀单已在 SeckillOrderService.createWithEvent 中落库， 此处只
     * 需确认并返回 COMMIT。
     *
     * @param msg 消息
     * @param arg 事务回调参数（固定为 null，业务主键从 header 读取）
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String orderNo = (String) msg.getHeaders().get(TxMessageHeader.EVENT_ID);
        try {
            // 秒杀单已在 createWithEvent 中落库，此处确认即可
            SeckillOrderDO order = orderMapper.selectByOrderNo(orderNo);
            if (order != null) {
                log.info("秒杀建单事务消息本地事务执行成功 orderNo={}", orderNo);
                return RocketMQLocalTransactionState.COMMIT;
            }
            log.warn("秒杀建单事务消息本地事务：秒杀单不存在 orderNo={}，回滚", orderNo);
            return RocketMQLocalTransactionState.ROLLBACK;
        } catch (Exception e) {
            log.error("秒杀建单事务消息本地事务执行失败 orderNo={}", orderNo, e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 回查本地事务状态：查询秒杀单判断事务是否成功。
     *
     * @param msg 消息
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String orderNo = (String) msg.getHeaders().get(TxMessageHeader.EVENT_ID);
        SeckillOrderDO order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            log.info("事务消息回查：秒杀单存在 orderNo={}，提交", orderNo);
            return RocketMQLocalTransactionState.COMMIT;
        }
        log.info("事务消息回查：秒杀单不存在 orderNo={}，回滚", orderNo);
        return RocketMQLocalTransactionState.ROLLBACK;
    }
}
