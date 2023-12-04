package com.practice.cartms.Controller;

import com.practice.cartms.Dto.CartItemDto;
import com.practice.cartms.Entity.Cart;
import com.practice.cartms.Service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {
    @InjectMocks
    private CartController cartController;

    @Mock
    private CartService cartService;

    private CartItemDto cartItemDto;

    @Test
    public void testCreateCartSuccess() {
        Long cartId = 1L;

        when(cartService.createCart()).thenReturn(cartId);

        // Call the controller method
        ResponseEntity<Long> responseEntity = cartController.createCart();

        // Verify the response status and body
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1L, responseEntity.getBody());

        // Verify that the cartService.createCart method was called
        verify(cartService, times(1)).createCart();
    }

    @Test
    public void testCreateCartFailure() {
        // Simulate a failure scenario by throwing an exception
        when(cartService.createCart()).thenThrow(new RuntimeException("Cart creation failed"));

        // Call the controller method
        ResponseEntity<Long> responseEntity = cartController.createCart();

        // Verify the response status and body for failure scenario
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());  // Assuming the body is null for failure

        // Verify that the cartService.createCart method was called
        verify(cartService, times(1)).createCart();
    }

    //Retrieve Cart Content:
    @Test
    public void testViewCartById() {
        Long cartId = 1L;

        Cart cart = new Cart();
        cart.setCartId(cartId);

        when(cartService.getCartById(cartId)).thenReturn(Optional.of(cart));

        ResponseEntity<Cart> responseEntity = cartController.getCartById(cartId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(cart, responseEntity.getBody());

        verify(cartService, times(1)).getCartById(cartId);
    }

    @Test
    public void testGetCartByIdNotFound() {
        Long cartId = 2L;

        when(cartService.getCartById(cartId)).thenReturn(Optional.empty());

        ResponseEntity<Cart> responseEntity = cartController.getCartById(cartId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());

        verify(cartService, times(1)).getCartById(cartId);
    }

    @Test
    public void testAddCartItemSuccess() {
        // Mocking behavior for the cart service to return an Optional with a known Cart
        Long cartId = 1L;
        Cart cart = new Cart();

        when(cartService.getCartById(cartId)).thenReturn(Optional.of(cart));


        // Call the addCartItem method with valid cartItemDto and cartId
        ResponseEntity<String> response = cartController.addCartItem(cartItemDto, cartId);

        // Verify that the cartService.addCartItem method was called
        verify(cartService, times(1)).addCartItem(cartItemDto, cart);

        // Assert that the method returns an OK response with the expected message
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cart item added successfully.", response.getBody());
    }

    //Validate Input Data:
    @Test
    public void testAddCartItemFailureExceptionInService() {
        // Mocking behavior for the cart service to throw an exception
        Long cartId = 1L;
        Cart cart = new Cart();
        when(cartService.getCartById(cartId)).thenReturn(Optional.of(cart));
        doThrow(new RuntimeException("Some unexpected error")).when(cartService).addCartItem(any(), any());

        // Call the addCartItem method with valid cartItemDto and cartId
        ResponseEntity<String> response = cartController.addCartItem(cartItemDto, cartId);

        // Verify that the cartService.addCartItem method was called
        verify(cartService, times(1)).addCartItem(cartItemDto, cart);

        // Assert that the method returns a 500 internal server error response with the expected message
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error adding cart item: Some unexpected error", response.getBody());
    }

    //testcase:Total Price Update (Remove Item):
    @Test
    public void testRemoveCartItemAndUpdateTotalPrice() {
        // Arrange
        Long cartItemId = 1L;
        int quantityToRemove = 2;

        // Mock the behavior of cartService
        doNothing().when(cartService).removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove);

        // Act
        ResponseEntity<String> response = cartController.removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cart item removed successfully.", response.getBody());

        // Verify that the method was called with the correct parameters
        verify(cartService, times(1)).removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove);
    }
    @Test
    public void testRemoveCartItemAndUpdateTotalPriceError() {
        // Arrange
        Long cartItemId = 1L;
        int quantityToRemove = 2;

        // Mock the behavior of cartService to throw an exception
        doThrow(new RuntimeException("Some error")).when(cartService).removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove);

        // Act
        ResponseEntity<String> response = cartController.removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error removing cart item: Some error", response.getBody());

        // Verify that the method was called with the correct parameters
        verify(cartService, times(1)).removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove);
    }



}
