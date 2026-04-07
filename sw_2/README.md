# High Concurrency Read Application

This is a demo application for high concurrency read operations, featuring:

- Spring Boot backend with JPA and Redis caching
- Load balancing with Nginx
- Static content serving with Nginx
- Docker containerization

## Project Structure

```
├── Dockerfile              # Dockerfile for Spring Boot application
├── docker-compose.yml      # Docker Compose configuration
├── nginx.conf              # Nginx configuration
├── pom.xml                 # Maven project configuration
├── src/                    # Source code
│   ├── main/java/com/example/
│   │   ├── HighConcurrencyReadApplication.java  # Main application class
│   │   ├── config/DataInitializer.java          # Data initialization
│   │   ├── controller/ProductController.java    # REST controller
│   │   ├── entity/Product.java                  # Product entity
│   │   ├── repository/ProductRepository.java    # JPA repository
│   │   └── service/ProductService.java          # Service with caching
│   └── main/resources/application.properties    # Application properties
└── static/                 # Static files
    └── index.html          # Simple HTML page
```

## How to Run

### Prerequisites

- Docker and Docker Compose installed
- JDK 17 or higher
- Maven

### Steps

1. Build and start all services using Docker Compose:

```bash
docker-compose up -d
```

2. Wait for all services to start. The following services will be available:

- Nginx load balancer: http://localhost:80
- Spring Boot app 1: http://localhost:8081
- Spring Boot app 2: http://localhost:8082
- MySQL database: localhost:3306
- Redis cache: localhost:6379

3. Access the static content at http://localhost:80

4. Access the API endpoints:

- Get product by ID: http://localhost:80/api/products/{id}
- Create product: POST http://localhost:80/api/products
- Delete product: DELETE http://localhost:80/api/products/{id}

## Load Balancing

Nginx is configured to use round-robin load balancing between the two Spring Boot instances. You can modify the nginx.conf file to use different load balancing algorithms:

- Round Robin (default): Distributes requests equally
- Least Connections: Sends requests to the server with the fewest active connections
- IP Hash: Uses the client IP address to determine which server to use

## Caching

The application uses Redis for caching product details. It includes:

- Cache penetration prevention: Caches null values for non-existent products
- Cache expiration: Products are cached for 1 hour
- Null value caching: Null values are cached for 1 minute

## Testing with JMeter

1. Download and install JMeter from https://jmeter.apache.org/

2. Create a test plan with the following elements:

   - Thread Group: Set the number of threads and ramp-up period
   - HTTP Request: Set the server name to localhost and port to 80
   - HTTP Request Path: /api/products/1 (for testing product retrieval)
   - Listener: Add a Summary Report to view results

3. Run the test and observe the response times

4. Check the backend logs to verify that requests are distributed evenly between the two Spring Boot instances:

```bash
docker logs spring-app-1
docker logs spring-app-2
```

## Static Content Testing

To test static content performance:

1. Create a JMeter test plan with HTTP requests to http://localhost:80
2. Run the test and compare response times with API requests
