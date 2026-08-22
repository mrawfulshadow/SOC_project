package com.soc.productservice.service;

import com.soc.productservice.model.Product;
import com.soc.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;

    public List<Product> getAllProducts() { return repository.findAll(); }
    public Product getProductById(String id) { return repository.findById(id).orElse(null); }
    public Product createProduct(Product product) { return repository.save(product); }
    public void deleteProduct(String id) { repository.deleteById(id); }
}

