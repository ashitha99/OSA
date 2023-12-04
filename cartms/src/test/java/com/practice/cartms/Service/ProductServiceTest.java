package com.practice.cartms.Service;

import com.practice.cartms.Entity.Product;
import com.practice.cartms.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    public void testCreateProduct() {
        // Arrange
        Product inputProduct = new Product();
        Product savedProduct = new Product();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        Product createdProduct = productService.createProduct(inputProduct);

        // Assert
        assertEquals(savedProduct, createdProduct);
        verify(productRepository, times(1)).save(any(Product.class));
    }
    @Test
    public void testCreateProduct_Failure() {
        // Arrange
        Product inputProduct = new Product();

        // Simulate an error during the saving process
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("Simulated save failure"));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> productService.createProduct(inputProduct));

        // Verify that productService.createProduct was called with the correct parameter
        verify(productRepository, times(1)).save(any(Product.class));
    }
    @Test
    public void testGetProductById_NonExistingProduct() {
        // Arrange
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act
        Product retrievedProduct = productService.getProductById(productId);

        // Assert
        assertNull(retrievedProduct);
        verify(productRepository, times(1)).findById(productId);
    }
    @Test
    public void testGetProductById_ExistingProduct() {
        // Arrange
        Long productId = 1L;
        Product expectedProduct = new Product();

        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

        // Act
        Product retrievedProduct = productService.getProductById(productId);

        // Assert
        assertNotNull(retrievedProduct);
        assertEquals(expectedProduct, retrievedProduct);
        verify(productRepository, times(1)).findById(productId);
    }

}
