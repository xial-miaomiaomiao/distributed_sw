## 项目结构

```
hw_1/
├── backend/              # Spring Boot 后端服务
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/
│   │       │   ├── Application.java
│   │       │   ├── config/
│   │       │   │   └── RedisConfig.java
│   │       │   ├── controller/
│   │       │   │   └── ProductController.java
│   │       │   ├── entity/
│   │       │   │   └── Product.java
│   │       │   ├── repository/
│   │       │   │   └── ProductRepository.java
│   │       │   └── service/
│   │       │       └── ProductService.java
│   │       └── resources/
│   │           └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── nginx/                # Nginx 配置
│   └── nginx.conf
├── static/               # 前端静态文件
│   ├── index.html
│   ├── style.css
│   └── app.js
├── jmeter/               # JMeter 测试脚本
│   └── load-test.jmx
└── docker-compose.yml    # Docker 编排文件
```

## 快速开始

### 1. 启动所有服务

```bash
cd hw_1
docker-compose up -d
```

### 2. 查看服务状态

```bash
docker-compose ps
```

### 3. 访问系统

- **前端页面**: http://localhost
- **后端API**: http://localhost/api/products/1
- **Nginx健康检查**: http://localhost/nginx-health

### 4. 停止服务

```bash
docker-compose down
```

## 功能说明

### 1. 负载均衡

Nginx 配置了三种负载均衡算法：

- **轮询（默认）**: `http://localhost/api/products/{id}`
- **权重**: `http://localhost/api/weighted/products/{id}`
- **IP哈希**: `http://localhost/api/iphash/products/{id}`

### 2. 动静分离

- 静态文件（HTML/CSS/JS）由 Nginx 直接返回
- API 请求转发到后端服务
- 静态资源缓存 30 天

### 3. 分布式缓存

Redis 缓存商品数据，提供四种查询策略：

| 接口 | 说明 |
|------|------|
| `/api/products/{id}` | 基础版本，存在缓存问题 |
| `/api/products/penetration/{id}` | 防止缓存穿透 |
| `/api/products/breakdown/{id}` | 防止缓存击穿 |
| `/api/products/avalanche/{id}` | 防止缓存雪崩 |

### 4. 缓存问题解决方案

#### 缓存穿透
- **问题**: 查询不存在的数据，直接打到数据库
- **解决**: 缓存空值（设置较短过期时间）

#### 缓存击穿
- **问题**: 热点缓存过期，大量请求同时打到数据库
- **解决**: 使用互斥锁，只有一个线程查询数据库

#### 缓存雪崩
- **问题**: 大量缓存同时过期，数据库压力剧增
- **解决**: 随机过期时间，避免同时失效

## JMeter 压力测试

### 测试场景

1. **负载均衡测试**: 10线程 × 100次循环，验证请求均匀分配
2. **静态文件测试**: 测试 Nginx 动静分离性能
3. **缓存穿透测试**: 查询不存在商品，验证防护效果

### 使用方法

1. 打开 JMeter
2. 导入 `jmeter/load-test.jmx`
3. 点击运行
4. 查看"汇总报告"和"查看结果树"

## 验证负载均衡

查看后端服务日志，验证请求是否均匀分配：

```bash
# 查看实例1日志
docker logs distributed_backend1

# 查看实例2日志
docker logs distributed_backend2
```

## 技术栈

- **后端**: Spring Boot 2.7 + JPA + MySQL + Redis
- **代理**: Nginx（负载均衡 + 动静分离）
- **容器**: Docker + Docker Compose
- **测试**: JMeter

## 端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx | 80 | 入口网关 |
| Backend1 | 8081 | 后端实例1 |
| Backend2 | 8082 | 后端实例2 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
