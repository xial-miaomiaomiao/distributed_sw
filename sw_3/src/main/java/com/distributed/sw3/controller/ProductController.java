package com.distributed.sw3.controller;

import com.distributed.sw3.model.Product;
import com.distributed.sw3.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public int saveProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @PutMapping
    public int updateProduct(@RequestBody Product product) {
        return productService.updateProduct(product);
    }

    @DeleteMapping("/{id}")
    public int deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
}