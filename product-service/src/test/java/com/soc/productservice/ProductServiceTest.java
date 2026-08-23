package com.soc.productservice;

import com.soc.productservice.controller.ProductController;
import com.soc.productservice.model.Product;
import com.soc.productservice.repository.ProductRepository;
import com.soc.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id("p1")
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .stockQuantity(10)
                .build();
    }

    @Test
    void testCreateProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product created = productService.createProduct(sampleProduct);
        assertNotNull(created);
        assertEquals("Test Product", created.getName());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<Product> products = productService.getAllProducts();
        assertEquals(1, products.size());
        assertEquals("p1", products.get(0).getId());
    }

    @Test
    void testGetProductById_Found() {
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));

        Product product = productService.getProductById("p1");
        assertNotNull(product);
        assertEquals("Test Product", product.getName());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById("p999")).thenReturn(Optional.empty());

        Product product = productService.getProductById("p999");
        assertNull(product);
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productRepository).deleteById("p1");

        productService.deleteProduct("p1");
        verify(productRepository, times(1)).deleteById("p1");
    }

    @Test
    void testController_CreateProduct_AdminAllowed() {
        // Wire productService to controller manually for controller unit test
        org.springframework.test.util.ReflectionTestUtils.setField(productController, "service", productService);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ResponseEntity<?> response = productController.create("ROLE_ADMIN", sampleProduct);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testController_CreateProduct_UserForbidden() {
        org.springframework.test.util.ReflectionTestUtils.setField(productController, "service", productService);

        ResponseEntity<?> response = productController.create("ROLE_USER", sampleProduct);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testController_DeleteProduct_AdminAllowed() {
        org.springframework.test.util.ReflectionTestUtils.setField(productController, "service", productService);
        doNothing().when(productRepository).deleteById("p1");

        ResponseEntity<?> response = productController.delete("ROLE_ADMIN", "p1");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testController_DeleteProduct_UserForbidden() {
        org.springframework.test.util.ReflectionTestUtils.setField(productController, "service", productService);

        ResponseEntity<?> response = productController.delete("ROLE_USER", "p1");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(productRepository, never()).deleteById(anyString());
    }
}
