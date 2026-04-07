package com.example.seckill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 成功响应
     */
    public static SeckillResponse success(String orderId) {
        return SeckillResponse.builder()
                .success(true)
                .message("秒杀成功")
                .orderId(orderId)
                .build();
    }

    /**
     * 失败响应
     */
    public static SeckillResponse fail(String message) {
        return SeckillResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
