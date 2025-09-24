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

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrPhone) throws UsernameNotFoundException {
        // Try loading application user first
        User user = userRepository.findByEmail(usernameOrPhone)
                .or(() -> userRepository.findByPhone(usernameOrPhone))
                .orElse(null);

        if (user != null) {
            return UserPrincipal.create(user);
        }

        // Fallback to admin directory if no user found
        Admin admin = adminRepository.findByEmail(usernameOrPhone)
                .or(() -> adminRepository.findByPhone(usernameOrPhone))
                .orElseThrow(() -> new UsernameNotFoundException("User/Admin not found with email/phone : " + usernameOrPhone));

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
