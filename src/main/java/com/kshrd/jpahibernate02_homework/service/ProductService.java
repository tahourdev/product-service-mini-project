package com.kshrd.jpahibernate02_homework.service;

import com.kshrd.jpahibernate02_homework.dto.request.ProductRequest;
import com.kshrd.jpahibernate02_homework.model.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductRequest productRequest);
    void deleteProduct(Long id);
    List<Product> getAllProduct(Integer page, Integer size);
    Product getProductById(Long id);
    Product updateProductById(Long id, ProductRequest productRequest);
    List<Product> searchProduct(Integer page, Integer size, String search);
    List<Product> filterProductByQuantity(Integer page, Integer size,Integer quantity);
    Integer countAllProducts();
    Integer countFilterProducts(Integer page, Integer size,Integer quantity);
    Integer countSearchProducts(Integer page, Integer size, String name);
}
