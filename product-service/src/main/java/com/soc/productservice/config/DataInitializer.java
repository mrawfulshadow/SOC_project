package com.soc.productservice.config;

import com.soc.productservice.model.Product;
import com.soc.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            log.info("Seeding initial products into product_db MongoDB...");
            List<Product> sampleProducts = List.of(
                    Product.builder()
                            .name("Gaming Laptop")
                            .description("High performance RTX 4070 gaming laptop")
                            .price(1200.0)
                            .stockQuantity(15)
                            .build(),
                    Product.builder()
                            .name("Smartphone X")
                            .description("Next-gen flagship smartphone with OLED display")
                            .price(799.0)
                            .stockQuantity(25)
                            .build(),
                    Product.builder()
                            .name("Wireless Headphones")
                            .description("Active Noise Cancelling Bluetooth headphones")
                            .price(150.0)
                            .stockQuantity(40)
                            .build()
            );
            productRepository.saveAll(sampleProducts);
            log.info("Initial products seeded successfully.");
        }
    }
}
