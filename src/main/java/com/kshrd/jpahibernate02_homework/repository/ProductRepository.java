package com.kshrd.jpahibernate02_homework.repository;

import com.kshrd.jpahibernate02_homework.dto.request.ProductRequest;
import com.kshrd.jpahibernate02_homework.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class ProductRepository {
    @PersistenceContext
    private EntityManager em;


    public Product save(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .build();
//        em.persist(productRequest.toEntity());
        em.persist(product);
        return product;
    }

    public List<Product> getAllProduct(Integer page, Integer size) {
        return em.createQuery("select p from products p", Product.class)
                .setFirstResult(page-1)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Product> searchProduct(Integer page, Integer size, String name) {
        return em.createQuery("select p from products p where p.name ilike concat('%', :name, '%') ", Product.class)
                .setFirstResult(page-1)
                .setMaxResults(size)
                .setParameter("name", name).getResultList();
    }

    public List<Product> filterProductByQuantity(Integer page, Integer size, Integer q) {
        return em.createQuery("select p from products p where p.quantity < :q ", Product.class)
                .setFirstResult(page-1)
                .setMaxResults(size)
                .setParameter("q", q).getResultList();
    }

    public void deleteProduct(Long id) {
        Product product = em.find(Product.class, id);
        em.remove(product);
    }

    public Integer countAllProducts() {
        Long num = (Long) em.createQuery("select count(p) from products p ").getSingleResult();
        return num.intValue();
    }

    public Integer countSearchProducts(Integer page, Integer size, String name) {
        Long num = (Long) em.createQuery("select count(p) from products p where p.name ilike concat('%', :name, '%') ")
                .setFirstResult(page-1)
                .setMaxResults(size)
                .setParameter("name", name)
                .getSingleResult();
        return num.intValue();
    }

    public Integer countFilterProducts(Integer page, Integer size, Integer q) {
        Long num = (Long) em.createQuery("select count(p) from products p where p.quantity < :q ")
                .setFirstResult(page - 1)
                .setMaxResults(size)
                .setParameter("q", q).getSingleResult();
        return num.intValue();
    }

    public Optional<Product> getProductById(Long id) {
        return Optional.ofNullable(em.find(Product.class, id));
    }

    public Product updateProduct(Long id, ProductRequest productRequest) {
        Product product = em.find(Product.class, id);
        em.detach(product);
        product.setName(productRequest.getName());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        em.merge(product);
        return product;
    }
}
