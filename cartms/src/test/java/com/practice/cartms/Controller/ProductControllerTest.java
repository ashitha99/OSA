package com.practice.cartms.Controller;

import com.practice.cartms.Entity.Product;
import com.practice.cartms.Service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    @Test
    public void testCreateProduct() {
        // Arrange
        Long productId = 1L;
        String productName = "pen";

        Product inputProduct = new Product();

        when(productService.createProduct(eq(inputProduct))).thenReturn(inputProduct);

        // Act
        ResponseEntity<Product> response = productController.createProduct(inputProduct);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(inputProduct, response.getBody());

        // Verify that productService.createProduct was called with the correct parameter
        verify(productService, times(1)).createProduct(eq(inputProduct));
    }
    @Test
    public void testCreateProduct_Exception() {
        // Arrange
        Product inputProduct = new Product(/* provide necessary input values */);

        // Simulate an exception during the product creation process
        when(productService.createProduct(any(Product.class))).thenThrow(new RuntimeException("Simulated createProduct exception"));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> productController.createProduct(inputProduct));

        // Verify that productService.createProduct was called with the correct parameter
        verify(productService, times(1)).createProduct(any(Product.class));
    }
    @Test
    public void testGetProductById_ExistingProduct() {
        // Arrange
        Long productId = 1L;
        Product expectedProduct = new Product(/* provide necessary expected product values */);

        when(productService.getProductById(productId)).thenReturn(expectedProduct);

        // Act
        ResponseEntity<Product> responseEntity = productController.getProductById(productId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedProduct, responseEntity.getBody());
        verify(productService, times(1)).getProductById(productId);
    }
    @Test
    public void testGetProductById_NonExistingProduct() {
        // Arrange
        Long productId = 2L;  // assuming 2 is a non-existing product ID

        when(productService.getProductById(productId)).thenReturn(null);

        // Act
        ResponseEntity<Product> responseEntity = productController.getProductById(productId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        verify(productService, times(1)).getProductById(productId);
    }
}
