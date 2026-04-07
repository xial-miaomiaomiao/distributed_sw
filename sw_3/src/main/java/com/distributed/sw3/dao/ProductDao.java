package com.distributed.sw3.dao;

import com.distributed.sw3.model.Product;

public interface ProductDao {
    Product getProductById(Long id);
    int saveProduct(Product product);
    int updateProduct(Product product);
    int deleteProduct(Long id);
}