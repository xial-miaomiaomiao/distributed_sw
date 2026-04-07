package com.example.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 扣减库存
     */
    @Update("UPDATE t_product SET seckill_stock = seckill_stock - #{quantity}, " +
            "update_time = NOW() WHERE id = #{productId} AND seckill_stock >= #{quantity}")
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 查询商品库存
     */
    @Select("SELECT seckill_stock FROM t_product WHERE id = #{productId}")
    Integer selectStock(@Param("productId") Long productId);
}
