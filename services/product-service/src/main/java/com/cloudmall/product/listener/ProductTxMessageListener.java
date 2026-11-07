package com.cloudmall.product.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import com.cloudmall.product.model.entity.ProductDO;
import com.cloudmall.product.mapper.ProductMapper;
import com.cloudmall.rocketmq.producer.TxMessageHeader;

/**
 * 商品事务消息监听器
 * <p>
 * 处理商品事件的本地事务执行和状态回查。
 * <ul>
 *   <li>executeLocalTransaction：商品已在 ProductService 中落库，此处直接 COMMIT</li>
 *   <li>checkLocalTransaction：查询商品表判断事务是否成功</li>
 * </ul>
 *
 * @author : Tomatos
 */
@Slf4j
@RequiredArgsConstructor
@RocketMQTransactionListener
public class ProductTxMessageListener implements RocketMQLocalTransactionListener {

    private final ProductMapper productMapper;

    /**
     * 执行本地事务。 <p>
     * 商品已在 ProductService 中落库，此处只需确认并返回 COMMIT。
     *
     * @param msg 消息
     * @param arg 事务回调参数（固定为 null，业务主键从 header 读取）
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String productIdStr = (String) msg.getHeaders().get(TxMessageHeader.EVENT_ID);
        try {
            Long productId = Long.parseLong(productIdStr);
            ProductDO product = productMapper.selectById(productId);
            if (product != null) {
                log.info("商品事务消息本地事务执行成功 productId={}", productId);
                return RocketMQLocalTransactionState.COMMIT;
            }
            log.warn("商品事务消息本地事务：商品不存在 productId={}，回滚", productId);
            return RocketMQLocalTransactionState.ROLLBACK;
        } catch (Exception e) {
            log.error("商品事务消息本地事务执行失败 productId={}", productIdStr, e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 回查本地事务状态：查询商品表判断事务是否成功。
     *
     * @param msg 消息
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String productIdStr = (String) msg.getHeaders().get(TxMessageHeader.EVENT_ID);
        try {
            Long productId = Long.parseLong(productIdStr);
            ProductDO product = productMapper.selectById(productId);
            if (product != null) {
                log.info("事务消息回查：商品存在 productId={}，提交", productId);
                return RocketMQLocalTransactionState.COMMIT;
            }
            log.info("事务消息回查：商品不存在 productId={}，回滚", productId);
            return RocketMQLocalTransactionState.ROLLBACK;
        } catch (Exception e) {
            log.error("事务消息回查失败 productId={}", productIdStr, e);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
