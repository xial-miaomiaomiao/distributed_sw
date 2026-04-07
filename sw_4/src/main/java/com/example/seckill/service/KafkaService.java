package com.example.seckill.service;

import com.example.seckill.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/**
 * Kafka消息服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送订单消息
     */
    public void sendOrderMessage(String orderId, Long userId, Long productId, Integer quantity) {
        String message = orderId + ":" + userId + ":" + productId + ":" + quantity;
        
        kafkaTemplate.send(KafkaConfig.TOPIC_SECURITY_ORDER, orderId, message)
                .addCallback(
                        result -> log.info("Sent order message successfully: {}, partition: {}, offset: {}",
                                message,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()),
                        ex -> log.error("Failed to send order message: {}", message, ex)
                );
    }

    /**
     * 同步发送消息（等待结果）
     */
    public boolean sendOrderMessageSync(String orderId, Long userId, Long productId, Integer quantity) {
        String message = orderId + ":" + userId + ":" + productId + ":" + quantity;
        
        try {
            SendResult<String, String> result = kafkaTemplate.send(KafkaConfig.TOPIC_SECURITY_ORDER, orderId, message).get();
            log.info("Sent order message synchronously: {}, partition: {}, offset: {}",
                    message,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            return true;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send order message synchronously: {}", message, e);
            return false;
        }
    }
}
