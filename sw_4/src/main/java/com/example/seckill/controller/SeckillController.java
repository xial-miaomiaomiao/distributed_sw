package com.example.seckill.controller;

import com.example.seckill.dto.SeckillRequest;
import com.example.seckill.dto.SeckillResponse;
import com.example.seckill.entity.Order;
import com.example.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 秒杀控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
@Validated
public class SeckillController {

    private final SeckillService seckillService;

    /**
     * 执行秒杀
     */
    @PostMapping("/execute")
    public ResponseEntity<SeckillResponse> executeSeckill(@Valid @RequestBody SeckillRequest request) {
        log.info("Received seckill request: {}", request);
        SeckillResponse response = seckillService.executeSeckill(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 查询用户订单列表
     */
    @GetMapping("/orders/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        log.info("Query orders for user: {}", userId);
        List<Order> orders = seckillService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        log.info("Query order: {}", orderId);
        Order order = seckillService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * 支付订单
     */
    @PostMapping("/order/{orderId}/pay")
    public ResponseEntity<Boolean> payOrder(@PathVariable String orderId) {
        log.info("Pay order: {}", orderId);
        boolean success = seckillService.payOrder(orderId);
        return ResponseEntity.ok(success);
    }

    /**
     * 取消订单
     */
    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<Boolean> cancelOrder(@PathVariable String orderId) {
        log.info("Cancel order: {}", orderId);
        boolean success = seckillService.cancelOrder(orderId);
        return ResponseEntity.ok(success);
    }

    /**
     * 查询秒杀商品库存
     */
    @GetMapping("/stock/{productId}")
    public ResponseEntity<Integer> getSeckillStock(@PathVariable Long productId) {
        log.info("Query stock for product: {}", productId);
        Integer stock = seckillService.getSeckillStock(productId);
        return ResponseEntity.ok(stock);
    }

    /**
     * 初始化秒杀库存
     */
    @PostMapping("/stock/{productId}/init")
    public ResponseEntity<Boolean> initSeckillStock(@PathVariable Long productId) {
        log.info("Init stock for product: {}", productId);
        seckillService.initSeckillStock(productId);
        return ResponseEntity.ok(true);
    }
}
