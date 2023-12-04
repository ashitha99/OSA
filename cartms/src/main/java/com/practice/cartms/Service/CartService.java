package com.practice.cartms.Service;

import com.practice.cartms.Dto.CartItemDto;
import com.practice.cartms.Entity.Cart;
import com.practice.cartms.Entity.CartItem;
import com.practice.cartms.Entity.Product;
import com.practice.cartms.Repository.CartItemRepository;
import com.practice.cartms.Repository.CartRepository;
import com.practice.cartms.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartItemService cartItemService;

    public Long createCart() {
        try {

            Cart cart = new Cart();
            cart.setTotalPrice(0.0);
            Cart savedcart = cartRepository.save(cart);
            return savedcart.getCartId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save cart " + e.getMessage());
        }
    }

    public Optional<Cart> getCartById(Long cartId) {
        return cartRepository.findById(cartId);
    }

    public void addCartItem(CartItemDto cartItemDto, Cart cart) {
        // Retrieve the product
        Product product = productService.getProductById(cartItemDto.getProductId());

        if (product != null) {
            // Check if the product is already in the cart
            CartItem existingCartItem = cartItemRepository.findByProduct(product);

            if (existingCartItem != null) {
                // If the product is already in the cart, update the quantity and recalculate subtotal
                existingCartItem.setQuantity(existingCartItem.getQuantity() + cartItemDto.getQuantity());
                existingCartItem.setPrice(cartItemDto.getPrice());

                // Calculate subtotal directly
                existingCartItem.setSubtotal(existingCartItem.getQuantity() * existingCartItem.getPrice());

                cartItemRepository.save(existingCartItem);
            } else {
                // Create a new cart item
                CartItem newCartItem = new CartItem();
                newCartItem.setProduct(product);
                newCartItem.setCart(cart);
                newCartItem.setQuantity(cartItemDto.getQuantity());
                newCartItem.setPrice(cartItemDto.getPrice());

                // Calculate subtotal directly
                newCartItem.setSubtotal(newCartItem.getQuantity() * newCartItem.getPrice());

                // Save the new cart item
                cartItemRepository.save(newCartItem);
            }
            // Update the total price of the cart
            cartItemService.updateTotalPrice(Optional.ofNullable(cart));
        } else {
            throw new IllegalArgumentException("Product with ID " + cartItemDto.getProductId() + " not found");
        }
    }

    public void removeCartItemAndUpdateTotalPrice(Long cartItemId, int quantityToRemove) {
        try {
            CartItem cartItemToRemove = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new IllegalArgumentException("CartItem not found for ID: " + cartItemId));

            // Fetch the associated cart
            Cart cart = cartItemToRemove.getCart();


            // Update the quantity to remove
            int remainingQuantity = cartItemToRemove.getQuantity() - quantityToRemove;

            // Remove the cart item if the remaining quantity is zero or negative
            if (remainingQuantity <= 0) {
                cart.getCartItems().remove(cartItemToRemove);
                cartItemRepository.delete(cartItemToRemove);
            } else {
                // Update the quantity and calculate the new subtotal
                cartItemToRemove.setQuantity(remainingQuantity);
                cartItemToRemove.setSubtotal(remainingQuantity * cartItemToRemove.getPrice());
            }

            // Update the total price based on the changes
            double totalCartPrice = cart.getCartItems().stream().mapToDouble(CartItem::getSubtotal).sum();
            cart.setTotalPrice(totalCartPrice);

            // Save the updated cart
            cartRepository.save(cart);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while removing cart item and updating total price.", e);
        }



    }

}


