package com.ecommerce.config;

import com.ecommerce.model.User;
import com.ecommerce.model.Admin;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository, AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            boolean hasAdminUser = userRepository.findAll().stream()
                    .anyMatch(u -> u.getRole() == User.UserRole.ROLE_ADMIN);
            if (!hasAdminUser) {
                User admin = new User();
                admin.setFullName("Admin User");
                admin.setEmail("admin@shop.local");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.UserRole.ROLE_ADMIN);
                userRepository.save(admin);
                System.out.println("[DataInitializer] Seeded default user admin: admin@shop.local / admin123");
            }

            boolean hasAdminRow = adminRepository.count() > 0;
            if (!hasAdminRow) {
                Admin a = new Admin();
                a.setFullName("Platform Admin");
                a.setEmail("platform-admin@shop.local");
                adminRepository.save(a);
                System.out.println("[DataInitializer] Seeded admins table with: platform-admin@shop.local");
            }
        };
    }
}
