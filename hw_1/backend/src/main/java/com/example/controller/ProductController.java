package com.example.controller;

import com.example.entity.Product;
import com.example.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;
    
    @Value("${server.port}")
    private String serverPort;
    
    // Constructor
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * 基础版本 - 存在缓存穿透问题
     */
    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        log.info("收到请求，商品ID: {}, 服务端口: {}", id, serverPort);
        
        long startTime = System.currentTimeMillis();
        Product product = productService.getProductBasic(id);
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", product != null);
        result.put("data", product);
        result.put("serverPort", serverPort);
        result.put("responseTime", (endTime - startTime) + "ms");
        
        return result;
    }
    
    /**
     * 解决缓存穿透
     */
    @GetMapping("/penetration/{id}")
    public Map<String, Object> getProductPreventPenetration(@PathVariable Long id) {
        log.info("【防穿透】收到请求，商品ID: {}, 服务端口: {}", id, serverPort);
        
        long startTime = System.currentTimeMillis();
        Product product = productService.getProductPreventPenetration(id);
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", product != null);
        result.put("data", product);
        result.put("serverPort", serverPort);
        result.put("responseTime", (endTime - startTime) + "ms");
        result.put("strategy", "防止缓存穿透");
        
        return result;
    }
    
    /**
     * 解决缓存击穿
     */
    @GetMapping("/breakdown/{id}")
    public Map<String, Object> getProductPreventBreakdown(@PathVariable Long id) {
        log.info("【防击穿】收到请求，商品ID: {}, 服务端口: {}", id, serverPort);
        
        long startTime = System.currentTimeMillis();
        Product product = productService.getProductPreventBreakdown(id);
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", product != null);
        result.put("data", product);
        result.put("serverPort", serverPort);
        result.put("responseTime", (endTime - startTime) + "ms");
        result.put("strategy", "防止缓存击穿");
        
        return result;
    }
    
    /**
     * 解决缓存雪崩
     */
    @GetMapping("/avalanche/{id}")
    public Map<String, Object> getProductPreventAvalanche(@PathVariable Long id) {
        log.info("【防雪崩】收到请求，商品ID: {}, 服务端口: {}", id, serverPort);
        
        long startTime = System.currentTimeMillis();
        Product product = productService.getProductPreventAvalanche(id);
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", product != null);
        result.put("data", product);
        result.put("serverPort", serverPort);
        result.put("responseTime", (endTime - startTime) + "ms");
        result.put("strategy", "防止缓存雪崩");
        
        return result;
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        result.put("port", serverPort);
        return result;
    }
}
