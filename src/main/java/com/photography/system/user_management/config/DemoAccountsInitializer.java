package com.photography.system.user_management.config;

import com.photography.system.user_management.model.Role;
import com.photography.system.user_management.model.User;
import com.photography.system.user_management.repository.RoleRepository;
import com.photography.system.user_management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoAccountsInitializer {

    @Bean
    CommandLineRunner demoAccounts(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            ensureUser(userRepository, roleRepository, passwordEncoder,
                    "ROLE_ADMIN",
                    "Admin User",
                    "admin@gmail.com",
                    "0770000001",
                    "Admin@12345"
            );

            ensureUser(userRepository, roleRepository, passwordEncoder,
                    "ROLE_CUSTOMER",
                    "Customer User",
                    "customer@gmail.com",
                    "0770000002",
                    "Customer@12345"
            );

            ensureUser(userRepository, roleRepository, passwordEncoder,
                    "ROLE_PHOTOGRAPHER",
                    "Photographer User",
                    "photographer@gmail.com",
                    "0770000003",
                    "Photo@12345"
            );
        };
    }

    private static void ensureUser(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            String roleName,
            String fullName,
            String email,
            String phone,
            String rawPassword
    ) {
        if (userRepository.existsByEmail(email)) return;

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
    }
}
