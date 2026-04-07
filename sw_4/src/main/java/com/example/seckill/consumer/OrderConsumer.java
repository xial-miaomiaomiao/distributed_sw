package com.example.seckill.consumer;

import com.alibaba.fastjson2.JSON;
import com.example.seckill.entity.Order;
import com.example.seckill.mapper.OrderMapper;
import com.example.seckill.config.KafkaConfig;
import com.example.seckill.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kafka订单消息消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    /**
     * 消费订单消息，创建订单
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_SECURITY_ORDER, groupId = "seckill-order-group")
    @Transactional(rollbackFor = Exception.class)
    public void consumeOrderMessage(String message) {
        log.info("Received order message: {}", message);

        try {
            String[] parts = message.split(":");
            if (parts.length != 4) {
                log.error("Invalid message format: {}", message);
                return;
            }

            String orderId = parts[0];
            Long userId = Long.parseLong(parts[1]);
            Long productId = Long.parseLong(parts[2]);
            Integer quantity = Integer.parseInt(parts[3]);

            // 创建订单
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setProductId(productId);
            order.setQuantity(quantity);
            order.setStatus(0); // 待支付
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            // 获取商品信息
            com.example.seckill.entity.Product product = productMapper.selectById(productId);
            if (product != null) {
                order.setProductName(product.getName());
                order.setTotalAmount(product.getPrice().multiply(new BigDecimal(quantity)));
            }

            // 插入订单
            int result = orderMapper.insert(order);
            
            if (result > 0) {
                log.info("Order created successfully: {}", orderId);
            } else {
                log.error("Failed to create order: {}", orderId);
                throw new RuntimeException("Failed to create order");
            }

        } catch (Exception e) {
            log.error("Error processing order message: {}", message, e);
            throw e; // 抛出异常以触发事务回滚
        }
    }
}
