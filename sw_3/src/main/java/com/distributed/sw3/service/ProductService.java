package com.distributed.sw3.service;

import com.distributed.sw3.dao.ProductDao;
import com.distributed.sw3.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final long CACHE_EXPIRE_TIME = 3600; // 1小时

    public Product getProductById(Long id) {
        // 尝试从缓存获取
        String key = PRODUCT_KEY_PREFIX + id;
        Product product = (Product) redisTemplate.opsForValue().get(key);
        if (product != null) {
            return product;
        }

        // 缓存穿透保护：设置空值
        if (redisTemplate.hasKey(key + ":empty")) {
            return null;
        }

        // 从数据库获取
        product = productDao.getProductById(id);
        if (product != null) {
            // 缓存数据
            redisTemplate.opsForValue().set(key, product, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        } else {
            // 缓存空值，防止缓存穿透
            redisTemplate.opsForValue().set(key + ":empty", "", 60, TimeUnit.SECONDS);
        }
        return product;
    }

    public int saveProduct(Product product) {
        int result = productDao.saveProduct(product);
        // 清除可能的缓存
        String key = PRODUCT_KEY_PREFIX + product.getId();
        redisTemplate.delete(key);
        redisTemplate.delete(key + ":empty");
        return result;
    }

    public int updateProduct(Product product) {
        int result = productDao.updateProduct(product);
        // 清除缓存
        String key = PRODUCT_KEY_PREFIX + product.getId();
        redisTemplate.delete(key);
        redisTemplate.delete(key + ":empty");
        return result;
    }

    public int deleteProduct(Long id) {
        int result = productDao.deleteProduct(id);
        // 清除缓存
        String key = PRODUCT_KEY_PREFIX + id;
        redisTemplate.delete(key);
        redisTemplate.delete(key + ":empty");
        return result;
    }
}