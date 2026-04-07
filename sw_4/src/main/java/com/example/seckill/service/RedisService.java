package com.example.seckill.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String ORDER_KEY_PREFIX = "seckill:order:";
    private static final String USER_ORDER_PREFIX = "seckill:user:order:";

    /**
     * 初始化库存到Redis
     */
    public void initStock(Long productId, Integer stock) {
        String key = STOCK_KEY_PREFIX + productId;
        redisTemplate.opsForValue().set(key, String.valueOf(stock));
        log.info("Initialized stock for product {}: {}", productId, stock);
    }

    /**
     * 获取库存
     */
    public Integer getStock(Long productId) {
        String key = STOCK_KEY_PREFIX + productId;
        String stock = redisTemplate.opsForValue().get(key);
        return stock != null ? Integer.parseInt(stock) : null;
    }

    /**
     * 预扣减库存（使用Lua脚本保证原子性）
     * 返回true表示扣减成功，false表示库存不足
     */
    public boolean preDeductStock(Long productId, Integer quantity) {
        String key = STOCK_KEY_PREFIX + productId;

        String luaScript = 
            "local stock = redis.call('get', KEYS[1]) " +
            "if stock == false then return -1 end " +
            "stock = tonumber(stock) " +
            "local quantity = tonumber(ARGV[1]) " +
            "if stock < quantity then return 0 end " +
            "redis.call('decrby', KEYS[1], quantity) " +
            "return 1";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Arrays.asList(key), String.valueOf(quantity));

        if (result == null || result == -1) {
            log.warn("Product {} stock not found in Redis", productId);
            return false;
        }

        boolean success = result == 1;
        log.info("Pre-deduct stock for product {}, quantity {}, success: {}", productId, quantity, success);
        return success;
    }

    /**
     * 回滚库存
     */
    public void rollbackStock(Long productId, Integer quantity) {
        String key = STOCK_KEY_PREFIX + productId;
        redisTemplate.opsForValue().increment(key, quantity);
        log.info("Rolled back stock for product {}, quantity {}", productId, quantity);
    }

    /**
     * 标记用户已下单（幂等性控制）
     * 返回true表示标记成功（可以下单），false表示已存在（不能重复下单）
     */
    public boolean markUserOrdered(Long userId, Long productId) {
        String key = USER_ORDER_PREFIX + userId + ":" + productId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", 24, TimeUnit.HOURS);
        log.info("Mark user {} ordered product {}, success: {}", userId, productId, success);
        return success != null && success;
    }

    /**
     * 检查用户是否已下单
     */
    public boolean isUserOrdered(Long userId, Long productId) {
        String key = USER_ORDER_PREFIX + userId + ":" + productId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 存储订单ID
     */
    public void saveOrderId(String orderId, Long userId, Long productId) {
        String key = ORDER_KEY_PREFIX + orderId;
        redisTemplate.opsForValue().set(key, userId + ":" + productId, 7, TimeUnit.DAYS);
    }

    /**
     * 获取订单信息
     */
    public String getOrderInfo(String orderId) {
        String key = ORDER_KEY_PREFIX + orderId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除订单缓存
     */
    public void deleteOrderId(String orderId) {
        String key = ORDER_KEY_PREFIX + orderId;
        redisTemplate.delete(key);
    }
}
