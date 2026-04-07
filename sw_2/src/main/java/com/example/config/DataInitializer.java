package com.example.config;

import com.example.entity.Product;
import com.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if products already exist
        if (productRepository.count() == 0) {
            // Create sample products
            Product product1 = new Product();
            product1.setName("Product 1");
            product1.setDescription("This is a sample product for testing purposes.");
            product1.setPrice(99.99);
            product1.setStock(100);
            productRepository.save(product1);

            Product product2 = new Product();
            product2.setName("Product 2");
            product2.setDescription("Another sample product for testing.");
            product2.setPrice(199.99);
            product2.setStock(50);
            productRepository.save(product2);

            Product product3 = new Product();
            product3.setName("Product 3");
            product3.setDescription("A third sample product for testing.");
            product3.setPrice(299.99);
            product3.setStock(75);
            productRepository.save(product3);

            System.out.println("Sample products initialized successfully!");
        }
    }
}