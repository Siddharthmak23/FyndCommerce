package com.fyndhacks.console.config;

import com.fyndhacks.console.entity.Product;
import com.fyndhacks.console.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedProducts(ProductRepository products) {
        return args -> {
            if (products.count() == 0) {
                products.saveAll(List.of(
                    new Product("Wireless Mouse", "Reliable wireless optical mouse", new BigDecimal("799.00"), 5),
                    new Product("Mechanical Keyboard", "Compact mechanical keyboard", new BigDecimal("2499.00"), 0),
                    new Product("USB-C Hub", "Multi-port USB-C adapter", new BigDecimal("1299.00"), 12),
                    new Product("Laptop Stand", "Ergonomic aluminium laptop stand", new BigDecimal("999.00"), 8),
                    new Product("Bluetooth Headphones", "Wireless over-ear headphones", new BigDecimal("3499.00"), 6),
                    new Product("HD Webcam", "1080p high-definition webcam", new BigDecimal("1899.00"), 10)
                ));
            }
        };
    }
}
