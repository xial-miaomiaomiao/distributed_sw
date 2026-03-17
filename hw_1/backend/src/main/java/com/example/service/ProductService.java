package com.example.service;

import com.example.entity.Product;
import com.example.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Constructor
    public ProductService(ProductRepository productRepository, RedisTemplate<String, Object> redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }
    
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String LOCK_KEY_PREFIX = "lock:product:";
    private static final long CACHE_TTL = 10; // 缓存10分钟
    private static final long LOCK_TTL = 10; // 锁10秒
    
    /**
     * 初始化数据
     */
    @PostConstruct
    public void init() {
        // 初始化一些商品数据
        for (int i = 1; i <= 100; i++) {
            Product product = new Product();
            product.setName("商品" + i);
            product.setDescription("这是商品" + i + "的详细描述");
            product.setPrice(new BigDecimal(100 + i));
            product.setStock(100);
            productRepository.save(product);
        }
        log.info("初始化 {} 条商品数据", 100);
    }
    
    /**
     * 获取商品详情 - 基础版本（存在缓存穿透问题）
     */
    public Product getProductBasic(Long id) {
        String key = PRODUCT_KEY_PREFIX + id;
        
        // 1. 从缓存查询
        Product product = (Product) redisTemplate.opsForValue().get(key);
        if (product != null) {
            log.info("从缓存获取商品: {}", id);
            return product;
        }
        
        // 2. 缓存未命中，查询数据库
        product = productRepository.findById(id).orElse(null);
        
        // 3. 放入缓存
        if (product != null) {
            redisTemplate.opsForValue().set(key, product, CACHE_TTL, TimeUnit.MINUTES);
            log.info("缓存商品: {}", id);
        }
        
        return product;
    }
    
    /**
     * 获取商品详情 - 解决缓存穿透（布隆过滤器/空值缓存）
     */
    public Product getProductPreventPenetration(Long id) {
        String key = PRODUCT_KEY_PREFIX + id;
        
        // 1. 从缓存查询
        Product product = (Product) redisTemplate.opsForValue().get(key);
        if (product != null) {
            // 如果是空对象标记，直接返回null
            if (product.getId() == null) {
                log.info("缓存命中空值，商品不存在: {}", id);
                return null;
            }
            log.info("从缓存获取商品: {}", id);
            return product;
        }
        
        // 2. 缓存未命中，查询数据库
        product = productRepository.findById(id).orElse(null);
        
        // 3. 放入缓存（包括空值）
        if (product != null) {
            redisTemplate.opsForValue().set(key, product, CACHE_TTL, TimeUnit.MINUTES);
            log.info("缓存商品: {}", id);
        } else {
            // 缓存空值，防止缓存穿透（设置较短的过期时间）
            Product emptyProduct = new Product();
            redisTemplate.opsForValue().set(key, emptyProduct, 1, TimeUnit.MINUTES);
            log.info("缓存空值，商品不存在: {}", id);
        }
        
        return product;
    }
    
    /**
     * 获取商品详情 - 解决缓存击穿（互斥锁）
     */
    public Product getProductPreventBreakdown(Long id) {
        String key = PRODUCT_KEY_PREFIX + id;
        String lockKey = LOCK_KEY_PREFIX + id;
        
        // 1. 从缓存查询
        Product product = (Product) redisTemplate.opsForValue().get(key);
        if (product != null) {
            if (product.getId() == null) {
                return null;
            }
            log.info("从缓存获取商品: {}", id);
            return product;
        }
        
        // 2. 缓存未命中，尝试获取锁
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TTL, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(locked)) {
            try {
                // 双重检查，防止多个线程都获取到锁
                product = (Product) redisTemplate.opsForValue().get(key);
                if (product != null) {
                    return product.getId() == null ? null : product;
                }
                
                // 查询数据库
                product = productRepository.findById(id).orElse(null);
                
                // 放入缓存
                if (product != null) {
                    redisTemplate.opsForValue().set(key, product, CACHE_TTL, TimeUnit.MINUTES);
                    log.info("缓存商品: {}", id);
                } else {
                    Product emptyProduct = new Product();
                    redisTemplate.opsForValue().set(key, emptyProduct, 1, TimeUnit.MINUTES);
                }
            } finally {
                // 释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 未获取到锁，短暂等待后重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getProductPreventBreakdown(id);
        }
        
        return product;
    }
    
    /**
     * 获取商品详情 - 解决缓存雪崩（随机过期时间）
     */
    public Product getProductPreventAvalanche(Long id) {
        String key = PRODUCT_KEY_PREFIX + id;
        
        // 1. 从缓存查询
        Product product = (Product) redisTemplate.opsForValue().get(key);
        if (product != null) {
            if (product.getId() == null) {
                return null;
            }
            log.info("从缓存获取商品: {}", id);
            return product;
        }
        
        // 2. 缓存未命中，查询数据库
        product = productRepository.findById(id).orElse(null);
        
        // 3. 放入缓存，使用随机过期时间防止雪崩
        if (product != null) {
            // 基础时间 + 随机时间（0-5分钟）
            long randomTTL = CACHE_TTL + (long)(Math.random() * 5);
            redisTemplate.opsForValue().set(key, product, randomTTL, TimeUnit.MINUTES);
            log.info("缓存商品: {}, TTL: {}分钟", id, randomTTL);
        } else {
            Product emptyProduct = new Product();
            redisTemplate.opsForValue().set(key, emptyProduct, 1, TimeUnit.MINUTES);
        }
        
        return product;
    }
}
