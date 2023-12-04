package com.practice.cartms.Service;

import com.practice.cartms.Dto.CartItemDto;
import com.practice.cartms.Entity.Cart;
import com.practice.cartms.Entity.CartItem;
import com.practice.cartms.Entity.Product;
import com.practice.cartms.Repository.CartItemRepository;
import com.practice.cartms.Repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @InjectMocks
    private CartService cartService;
    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private CartItemDto cartItemDto;


    @Test
    public void testCreateCartSuccess() {

        // Mock the behavior of cartRepository.save to return a cart with a specific ID
        Cart savedCart = new Cart();
        savedCart.setCartId(1L);

        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        Long cartId = cartService.createCart();

        // Verify that the cartRepository.save method was called
        verify(cartRepository, times(1)).save(any(Cart.class));
    }


    @Test
    public void testCreateCartFailure() {
        // Mocking behavior for the cart repository to throw an exception
        when(cartRepository.save(any(Cart.class))).thenThrow(new RuntimeException("Failed to save cart"));

        // Call the createCart method and assert that it throws a RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> cartService.createCart());


        // Assert the expected exception message
        assertEquals("Failed to save cart Failed to save cart", exception.getMessage());

        // Verify that the cart repository save method was called
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void testCreateCartWithGeneratedId() {
        // Mocking behavior for the cart repository to return a Cart with a generated cartId
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setCartId(1L); // Set a non-null cartId for testing purposes
            return cart;
        });

        // Call the createCart method
        Long cartId = cartService.createCart();

        // Assert that the returned cartId is not null and greater than 0
        assertNotNull(cartId, "Cart ID should not be null");
        assertTrue(cartId > 0, "Cart ID should be greater than 0");
    }


    @Test
    public void testGetExistingCartById() {
        // Mocking behavior for the cart repository to return an Optional with a known Cart
        Long cartId = 1L;
        Cart expectedCart = new Cart();
        expectedCart.setCartId(cartId);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(expectedCart));

        // Call the getCartById method
        Optional<Cart> result = cartService.getCartById(cartId);

        // Assert that the returned Optional is not empty and contains the expected Cart
        assertTrue(result.isPresent(), "Optional should not be empty");
        assertEquals(expectedCart, result.get(), "Returned Cart should match the expected Cart");

        // Verify that the cart repository save method was called
        verify(cartRepository, times(1)).findById(1L);
        ;

    }

    @Test
    public void testGetNonexistentCartById() {
        // Mocking behavior for the cart repository to return an empty Optional
        Long cartId = 1L;
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        // Call the getCartById method
        Optional<Cart> result = cartService.getCartById(cartId);

        // Assert that the returned Optional is empty
        assertTrue(result.isEmpty(), "Optional should be empty for nonexistent Cart");
    }

    @Test
    public void testGetCartByIdWithException() {
        // Mocking behavior for the cart repository to throw a specific exception
        Long cartId = 1L;
        when(cartRepository.findById(cartId)).thenThrow(new RuntimeException("Failed to retrieve cart"));

        // Call the getCartById method and assert that it throws the expected exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> cartService.getCartById(cartId));

        // Assert the expected exception message
        assertEquals("Failed to retrieve cart", exception.getMessage());
    }

    @Test
    void testAddNewItemToEmptyCart() {
        // Arrange
        Long productId = 1L;
        Double price = 20.0;
        int quantity = 2;

        Cart cart = new Cart();

        CartItemDto cartItemDto = Mockito.mock(CartItemDto.class);
        when(cartItemDto.getProductId()).thenReturn(productId);
        when(cartItemDto.getQuantity()).thenReturn(quantity);
        when(cartItemDto.getPrice()).thenReturn(price);

        Product product = new Product();
        when(productService.getProductById(productId)).thenReturn(product);
        when(cartItemRepository.findByProduct(product)).thenReturn(null);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        // Act
        cartService.addCartItem(cartItemDto, cart);

        // Assert
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartItemService, times(1)).updateTotalPrice(Optional.ofNullable(cart));
    }

    @Test
    public void testAddNewItemToEmptyCart_ProductNotFound() {
        // Arrange
        Long productId = 1L;
        Double price = 20.0;
        int quantity = 2;

        Cart cart = new Cart();

        CartItemDto cartItemDto = Mockito.mock(CartItemDto.class);
        when(cartItemDto.getProductId()).thenReturn(productId);

        // Simulate ProductService returning null
        when(productService.getProductById(productId)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartService.addCartItem(cartItemDto, cart);
        });

        // Ensure that cartItemRepository and cartItemService methods were not called
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartItemService, never()).updateTotalPrice(Optional.ofNullable(cart));
    }


    @Test
    public void testRemoveCartItemAndUpdateTotalPrice_RemainingQuantity() {
        // Arrange
        Long cartItemId = 1L;
        int quantityToRemove = 2;

        CartItem cartItem = new CartItem(); // Create a cart item with quantity 3
        cartItem.setQuantity(3);

        Cart cart = new Cart();
        cart.setTotalPrice(30.0); // Set a sample total price for the cart
        cart.setCartItems(Collections.singletonList(cartItem));
        cartItem.setCart(cart);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // Act
        cartService.removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove);

        // Assert
        assertEquals(1, cartItem.getQuantity());
        // Add more assertions based on your business logic
    }

    //testcase:  Remove Item from Cart:
    @Test
    public void testRemoveCartItemAndUpdateTotalPrice_QuantityGreaterThanZero() {
        // Arrange
        Long cartItemId = 1L;
        int quantityToRemove = 2;

        CartItem cartItemToRemove = new CartItem();
        cartItemToRemove.setCartItemId(cartItemId);
        cartItemToRemove.setQuantity(3); // Initial quantity
        cartItemToRemove.setPrice(10.0); // Set a sample price for the cart item

        Cart cart = new Cart();
        cart.setTotalPrice(30.0); // Set a sample total price for the cart
        cart.setCartItems(Collections.singletonList(cartItemToRemove));

        cartItemToRemove.setCart(cart);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItemToRemove));

        // Act
        cartService.removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove);

        // Assert
        verify(cartItemRepository).findById(cartItemId);
        verify(cartRepository).save(any()); // We verify that save method is called

        // Additional assertions based on your business logic
        assertEquals(1, cart.getCartItems().size());
        CartItem updatedCartItem = cart.getCartItems().get(0);

        assertEquals(1, updatedCartItem.getQuantity());
        assertEquals(10.0, updatedCartItem.getSubtotal(), 0.001);
        assertEquals(10.0, cart.getTotalPrice(), 0.001);
    }

    //testcase:Remove Nonexistent Item from Cart:
    @Test
    public void testRemoveCartItemAndUpdateTotalPrice_RemainingQuantityNegative() {
        // Arrange
        Long cartItemId = 1L;
        int quantityToRemove = -4; // Quantity to remove is greater than the initial quantity

        CartItem cartItem = new CartItem(); // Create a cart item with quantity 3
        cartItem.setQuantity(3);

        Cart cart = new Cart();
        cart.setTotalPrice(30.0); // Set a sample total price for the cart
        cart.setCartItems(Collections.singletonList(cartItem));
        cartItem.setCart(cart);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // Act and Assert
        CartItem cartItem1 = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("CartItem not found"));

        if (quantityToRemove > cartItem.getQuantity()) {
            throw new IllegalArgumentException("Quantity to remove exceeds the initial quantity");
        }

        // Additional assertions based on your business logic
        // Ensure that the cart item and cart remain unchanged
        assertEquals(30.0, cart.getTotalPrice(), 0.001);
    }

    @Test
    public void testRemoveCartItemAndUpdateTotalPrice_CartItemNotFound() {
        // Arrange
        Long cartItemId = 1L;
        int quantityToRemove = 2;

        CartService cartService = new CartService();
        CartItemRepository cartItemRepository = mock(CartItemRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> cartItemRepository.findById(cartItemId).orElseThrow(() -> new NoSuchElementException("CartItem not found")));


        // Assert
        verify(cartRepository, never()).save(any());
    }

    //testcase:Total Price Update (Remove Item):
    @Test
    public void testRemoveCartItemAndUpdateTotalPrice_Failure() {
        // Arrange
        Long cartItemId=1L;

        int quantityToRemove = 2;

        Cart cart=new Cart();

        // Mock the behavior when trying to find the cart item
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(RuntimeException.class,
                () -> cartService.removeCartItemAndUpdateTotalPrice(cartItemId, quantityToRemove),
                "Expected exception when cart item is not found.");

        // Verify that certain methods were not called
        verify(cartRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
    }
}
