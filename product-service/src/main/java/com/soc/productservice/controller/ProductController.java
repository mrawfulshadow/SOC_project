package com.soc.productservice.controller;

import com.soc.productservice.model.Product;
import com.soc.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/products", "/api/products"})
public class ProductController {

    @Autowired
    private ProductService service;

    @GetMapping
    public List<Product> getAll() { 
        return service.getAllProducts(); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable String id) { 
        Product product = service.getProductById(id);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @jakarta.validation.Valid @RequestBody Product product) { 
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only users with ROLE_ADMIN can create products");
        }
        return new ResponseEntity<>(service.createProduct(product), HttpStatus.CREATED); 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable String id) { 
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only users with ROLE_ADMIN can delete products");
        }
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}