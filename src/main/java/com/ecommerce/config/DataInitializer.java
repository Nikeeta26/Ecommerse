package com.ecommerce.config;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            boolean hasAdmin = userRepository.findAll().stream()
                    .anyMatch(u -> u.getRole() == User.UserRole.ROLE_ADMIN);
            if (!hasAdmin) {
                User admin = new User();
                admin.setFullName("Admin User");
                admin.setEmail("admin@shop.local");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.UserRole.ROLE_ADMIN);
                userRepository.save(admin);
                System.out.println("[DataInitializer] Seeded default admin: admin@shop.local / admin123");
            }
        };
    }
}
