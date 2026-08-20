package com.soc.productservice.controller;

import com.soc.productservice.model.Product;
import com.soc.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService service;

    @GetMapping
    public List<Product> getAll() { return service.getAllProducts(); }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) { return service.getProductById(id); }

    @PostMapping
    public Product create(@RequestBody Product product) { return service.createProduct(product); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.deleteProduct(id); }
}