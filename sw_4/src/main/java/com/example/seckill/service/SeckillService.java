package com.example.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill.dto.SeckillRequest;
import com.example.seckill.dto.SeckillResponse;
import com.example.seckill.entity.Order;
import com.example.seckill.entity.Product;
import com.example.seckill.mapper.OrderMapper;
import com.example.seckill.mapper.ProductMapper;
import com.example.seckill.utils.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillService {

    private final RedisService redisService;
    private final KafkaService kafkaService;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    /**
     * 执行秒杀
     * 1. 幂等性检查（Redis）
     * 2. 库存预扣减（Redis）
     * 3. 发送订单消息（Kafka）
     */
    public SeckillResponse executeSeckill(SeckillRequest request) {
        Long userId = request.getUserId();
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        log.info("Seckill request: userId={}, productId={}, quantity={}", userId, productId, quantity);

        // 1. 幂等性检查
        if (redisService.isUserOrdered(userId, productId)) {
            log.warn("User {} has already ordered product {}", userId, productId);
            return SeckillResponse.fail("您已购买过该商品，请勿重复购买");
        }

        // 2. 标记用户下单状态
        if (!redisService.markUserOrdered(userId, productId)) {
            log.warn("User {} marked as ordered failed for product {}", userId, productId);
            return SeckillResponse.fail("您已购买过该商品，请勿重复购买");
        }

        // 3. 预扣减库存
        boolean stockDeducted = redisService.preDeductStock(productId, quantity);
        if (!stockDeducted) {
            log.warn("Stock insufficient for product {}", productId);
            return SeckillResponse.fail("库存不足，秒杀失败");
        }

        try {
            // 4. 生成订单ID
            String orderId = snowflakeIdGenerator.generateOrderId();

            // 5. 发送订单消息到Kafka
            kafkaService.sendOrderMessage(orderId, userId, productId, quantity);

            log.info("Seckill success: orderId={}, userId={}, productId={}", orderId, userId, productId);
            return SeckillResponse.success(orderId);

        } catch (Exception e) {
            // 发生异常，回滚Redis库存
            log.error("Error in seckill process, rolling back stock", e);
            redisService.rollbackStock(productId, quantity);
            return SeckillResponse.fail("秒杀过程出错，请重试");
        }
    }

    /**
     * 初始化秒杀商品库存
     */
    public void initSeckillStock(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null && product.getSeckillStock() != null) {
            redisService.initStock(productId, product.getSeckillStock());
        }
    }

    /**
     * 查询用户订单列表
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderMapper.selectByUserId(userId);
    }

    /**
     * 查询订单详情
     */
    public Order getOrderById(String orderId) {
        return orderMapper.selectByOrderId(orderId);
    }

    /**
     * 支付订单
     */
    @Transactional
    public boolean payOrder(String orderId) {
        Order order = orderMapper.selectByOrderId(orderId);
        if (order == null) {
            log.warn("Order not found: {}", orderId);
            return false;
        }

        if (order.getStatus() != 0) {
            log.warn("Order status is not pending payment: {}", orderId);
            return false;
        }

        // 扣减数据库库存
        int result = productMapper.deductStock(order.getProductId(), order.getQuantity());
        if (result <= 0) {
            log.error("Failed to deduct stock from database for order: {}", orderId);
            return false;
        }

        // 更新订单状态
        order.setStatus(1); // 已支付
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("Order paid successfully: {}", orderId);
        return true;
    }

    /**
     * 取消订单
     */
    @Transactional
    public boolean cancelOrder(String orderId) {
        Order order = orderMapper.selectByOrderId(orderId);
        if (order == null) {
            log.warn("Order not found: {}", orderId);
            return false;
        }

        if (order.getStatus() != 0) {
            log.warn("Order status is not pending: {}", orderId);
            return false;
        }

        // 回滚Redis库存
        redisService.rollbackStock(order.getProductId(), order.getQuantity());

        // 更新订单状态
        order.setStatus(2); // 已取消
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("Order cancelled: {}", orderId);
        return true;
    }

    /**
     * 查询秒杀商品库存（从Redis）
     */
    public Integer getSeckillStock(Long productId) {
        return redisService.getStock(productId);
    }
}
