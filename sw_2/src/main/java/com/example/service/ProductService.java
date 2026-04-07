package com.example.service;

import com.example.entity.Product;
import com.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final long CACHE_TTL = 3600; // 1 hour
    private static final long NULL_CACHE_TTL = 60; // 1 minute for null values

    public Product getProductById(Long id) {
        // Try to get from cache first
        String cacheKey = PRODUCT_CACHE_PREFIX + id;
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        
        if (product != null) {
            return product;
        }
        
        // Try to get from database
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isPresent()) {
            product = productOptional.get();
            // Put in cache
            redisTemplate.opsForValue().set(cacheKey, product, CACHE_TTL, TimeUnit.SECONDS);
            return product;
        } else {
            // Cache null value to prevent cache penetration
            redisTemplate.opsForValue().set(cacheKey, null, NULL_CACHE_TTL, TimeUnit.SECONDS);
            return null;
        }
    }

    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        // Update cache
        String cacheKey = PRODUCT_CACHE_PREFIX + savedProduct.getId();
        redisTemplate.opsForValue().set(cacheKey, savedProduct, CACHE_TTL, TimeUnit.SECONDS);
        return savedProduct;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        // Remove from cache
        String cacheKey = PRODUCT_CACHE_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
}