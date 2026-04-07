package com.example.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据用户ID查询订单列表
     */
    @Select("SELECT * FROM t_order WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据订单ID查询订单
     */
    @Select("SELECT * FROM t_order WHERE order_id = #{orderId}")
    Order selectByOrderId(@Param("orderId") String orderId);

    /**
     * 检查用户是否已购买过该商品
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE user_id = #{userId} AND product_id = #{productId} AND status != 2")
    int checkUserOrdered(@Param("userId") Long userId, @Param("productId") Long productId);
}
