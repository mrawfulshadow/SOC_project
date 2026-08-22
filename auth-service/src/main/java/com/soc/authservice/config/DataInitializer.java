package com.soc.authservice.config;

import com.soc.authservice.model.ApiKey;
import com.soc.authservice.model.User;
import com.soc.authservice.repository.ApiKeyRepository;
import com.soc.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users into auth_db MongoDB...");
            User defaultUser = User.builder()
                    .username("john_doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .role("ROLE_USER")
                    .build();
            userRepository.save(defaultUser);

            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin123!"))
                    .role("ROLE_ADMIN")
                    .build();
            userRepository.save(adminUser);

            log.info("Default users initialized successfully (john_doe, admin).");
        }

        if (apiKeyRepository.count() == 0) {
            log.info("Seeding API Keys into auth_db MongoDB...");
            List<ApiKey> keys = List.of(
                    ApiKey.builder()
                            .service("product-service")
                            .apiKey("PRODUCT-SERVICE-SECRET-KEY")
                            .headerName("X-API-KEY")
                            .targetPort(8081)
                            .status("ACTIVE")
                            .description("Product Catalog Service API Key (Student 2)")
                            .createdAt(LocalDateTime.now())
                            .build(),
                    ApiKey.builder()
                            .service("order-service")
                            .apiKey("order-secret-key-123")
                            .headerName("X-API-KEY")
                            .targetPort(8082)
                            .status("ACTIVE")
                            .description("Order Management Service API Key (Student 3)")
                            .createdAt(LocalDateTime.now())
                            .build(),
                    ApiKey.builder()
                            .service("payment-service")
                            .apiKey("payment-secret-key-123")
                            .headerName("X-API-KEY")
                            .targetPort(8083)
                            .status("ACTIVE")
                            .description("Payment Processing Service API Key (Student 4)")
                            .createdAt(LocalDateTime.now())
                            .build(),
                    ApiKey.builder()
                            .service("notification-service")
                            .apiKey("notification-secret-key-123")
                            .headerName("X-API-KEY")
                            .targetPort(8085)
                            .status("ACTIVE")
                            .description("Notification Alert Service API Key (Student 5)")
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            apiKeyRepository.saveAll(keys);
            log.info("API Keys seeded successfully in auth_db.");
        }
    }
}

