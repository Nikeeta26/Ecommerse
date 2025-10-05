package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.model.Admin;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrPhone) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by username/phone: {}", usernameOrPhone);
        
        // Try loading application user first
        User user = userRepository.findByEmail(usernameOrPhone)
                .or(() -> {
                    logger.debug("User not found by email, trying by phone");
                    return userRepository.findByPhone(usernameOrPhone);
                })
                .orElse(null);

        if (user != null) {
            logger.debug("User found with ID: {}", user.getId());
            return UserPrincipal.create(user);
        }

        logger.debug("No regular user found, checking admin users");
        // Fallback to admin directory if no user found
        Admin admin = adminRepository.findByEmail(usernameOrPhone)
                .or(() -> {
                    logger.debug("Admin not found by email, trying by phone");
                    return adminRepository.findByPhone(usernameOrPhone);
                })
                .orElseThrow(() -> {
                    String error = "User/Admin not found with email/phone: " + usernameOrPhone;
                    logger.error(error);
                    return new UsernameNotFoundException(error);
                });

        logger.debug("Admin user found with ID: {}", admin.getId());
        return UserPrincipal.create(admin);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        // First check users
        return userRepository.findById(id)
            .<UserDetails>map(UserPrincipal::create)
            .orElseGet(() -> adminRepository.findById(id)
                .map(UserPrincipal::create)
                .orElseThrow(() -> new UsernameNotFoundException("User/Admin not found with id : " + id))
            );
    }
}
