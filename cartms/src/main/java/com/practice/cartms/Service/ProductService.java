package com.practice.cartms.Service;

import com.practice.cartms.Entity.Product;
import com.practice.cartms.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }


}
