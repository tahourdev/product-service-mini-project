package com.kshrd.jpahibernate02_homework.service.implementation;

import com.kshrd.jpahibernate02_homework.dto.request.ProductRequest;
import com.kshrd.jpahibernate02_homework.exception.BadRequestException;
import com.kshrd.jpahibernate02_homework.exception.NotFoundException;
import com.kshrd.jpahibernate02_homework.model.Product;
import com.kshrd.jpahibernate02_homework.repository.ProductRepository;
import com.kshrd.jpahibernate02_homework.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Product createProduct(ProductRequest productRequest) {
        if (productRequest.getQuantity() == 0) throw new BadRequestException("Can't be zero");
        if (productRequest.getPrice().doubleValue() <= 0) throw new BadRequestException("Can't be zero");
        return productRepository.save(productRequest);
    }

    @Override
    public void deleteProduct(Long id) {
        getProductById(id);
        productRepository.deleteProduct(id);
    }

    @Override
    public List<Product> getAllProduct(Integer page, Integer size) {
        return productRepository.getAllProduct(page, size);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.getProductById(id).orElseThrow(
                () -> new NotFoundException("Not found")
        );
    }

    @Override
    public Product updateProductById(Long id, ProductRequest productRequest) {
        getProductById(id);
        return productRepository.updateProduct(id, productRequest);
    }

    @Override
    public List<Product> searchProduct(Integer page, Integer size,String search) {
        return productRepository.searchProduct(page, size, search);
    }

    @Override
    public List<Product> filterProductByQuantity(Integer page, Integer size, Integer quantity) {
        return productRepository.filterProductByQuantity(page, size, quantity);
    }

    @Override
    public Integer countAllProducts() {
        return productRepository.countAllProducts();
    }

    @Override
    public Integer countFilterProducts(Integer page, Integer size, Integer quantity) {
        return productRepository.countFilterProducts(page, size, quantity);
    }

    @Override
    public Integer countSearchProducts(Integer page, Integer size, String name) {
        return productRepository.countSearchProducts(page, size, name);
    }
}
