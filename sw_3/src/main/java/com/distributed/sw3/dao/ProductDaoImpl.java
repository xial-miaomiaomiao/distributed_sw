package com.distributed.sw3.dao;

import com.distributed.sw3.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ProductDaoImpl implements ProductDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Product getProductById(Long id) {
        String sql = "SELECT * FROM product WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new ProductRowMapper());
    }

    @Override
    public int saveProduct(Product product) {
        String sql = "INSERT INTO product (name, description, price, stock) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, product.getName(), product.getDescription(), product.getPrice(), product.getStock());
    }

    @Override
    public int updateProduct(Product product) {
        String sql = "UPDATE product SET name = ?, description = ?, price = ?, stock = ? WHERE id = ?";
        return jdbcTemplate.update(sql, product.getName(), product.getDescription(), product.getPrice(), product.getStock(), product.getId());
    }

    @Override
    public int deleteProduct(Long id) {
        String sql = "DELETE FROM product WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    private class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product product = new Product();
            product.setId(rs.getLong("id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setPrice(rs.getDouble("price"));
            product.setStock(rs.getInt("stock"));
            return product;
        }
    }
}