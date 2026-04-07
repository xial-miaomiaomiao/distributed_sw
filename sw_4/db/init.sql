-- 创建数据库
CREATE DATABASE IF NOT EXISTS seckill DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seckill;

-- 商品表
CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description VARCHAR(500) COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    stock INT NOT NULL COMMENT '库存数量',
    seckill_stock INT NOT NULL COMMENT '秒杀库存',
    seckill_start_time DATETIME COMMENT '秒杀开始时间',
    seckill_end_time DATETIME COMMENT '秒杀结束时间',
    status TINYINT DEFAULT 1 COMMENT '商品状态：0-下架，1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_seckill_time (seckill_start_time, seckill_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    order_id VARCHAR(50) PRIMARY KEY COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    quantity INT NOT NULL COMMENT '购买数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    status TINYINT DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消，3-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    pay_time DATETIME COMMENT '支付时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 初始化测试数据
INSERT INTO t_product (name, description, price, stock, seckill_stock, seckill_start_time, seckill_end_time, status) VALUES
('iPhone 14 Pro', '苹果iPhone 14 Pro 128GB', 7999.00, 100, 10, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1),
('AirPods Pro 2', '苹果AirPods Pro 2代', 1899.00, 200, 20, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1),
('MacBook Pro 14', '苹果MacBook Pro 14英寸', 14999.00, 50, 5, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1);

-- 查看数据
SELECT * FROM t_product;
