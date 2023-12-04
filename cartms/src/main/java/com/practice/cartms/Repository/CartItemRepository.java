package com.practice.cartms.Repository;

import com.practice.cartms.Entity.CartItem;
import com.practice.cartms.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    CartItem findByProduct(Product product);
}
