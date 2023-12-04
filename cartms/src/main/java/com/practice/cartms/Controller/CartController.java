package com.practice.cartms.Controller;

import com.practice.cartms.Dto.CartItemDto;
import com.practice.cartms.Entity.Cart;
import com.practice.cartms.Service.CartItemService;
import com.practice.cartms.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/carts")
public class CartController {
    @Autowired
    private CartService cartService;  // Assuming you have a CartService

    @Autowired
    private CartItemService cartItemService;


    @PostMapping("/createCart")
    public ResponseEntity<Long> createCart() {
        try {
            Long cartId = cartService.createCart();
            return ResponseEntity.ok(cartId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    //ViewCart Endpoint to fetch the list of items in the cart.
    @GetMapping("/viewCart/{cartId}")
    public ResponseEntity<Cart> getCartById(@PathVariable Long cartId) {
        Optional<Cart> cart = cartService.getCartById(cartId);
        return cart.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping("/addCartItem/{cartId}")
    public ResponseEntity<String> addCartItem(
            @RequestBody CartItemDto cartItemDto,
            @PathVariable Long cartId) {
        try {
            // Fetch the cart by ID
            Optional<Cart> cart = cartService.getCartById(cartId);

            // Check if cart is present, then add the cart item to the cart and update total price
            cart.ifPresent(value -> cartService.addCartItem(cartItemDto, value));

            return ResponseEntity.ok("Cart item added successfully.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding cart item: " + e.getMessage());
        }
    }
    //Endpoint to remove the items from cart.
    @DeleteMapping("/removeCartItem/{cartItemId}/{quantityToRemove}")
    public ResponseEntity<String> removeCartItemAndUpdateTotalPrice(
            @PathVariable Long cartItemId,
            @PathVariable int quantityToRemove) {
        try {
            cartService.removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove);
            return ResponseEntity.ok("Cart item removed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing cart item: " + e.getMessage());
        }
    }

}
