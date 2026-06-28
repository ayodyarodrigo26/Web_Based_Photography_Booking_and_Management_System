package com.photography.system.user_management.config;

import com.photography.system.user_management.repository.UserRepository;
import com.photography.system.user_management.security.RolePermissions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;

@Configuration
public class SecurityConfig {

    private final RoleBasedSuccessHandler successHandler;

    public SecurityConfig(RoleBasedSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            var user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String role = user.getRole().getName();

            var authorities = new ArrayList<org.springframework.security.core.GrantedAuthority>();
            authorities.add(new SimpleGrantedAuthority(role));

            RolePermissions.forRole(role)
                    .forEach(p -> authorities.add(new SimpleGrantedAuthority(p.name())));

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    user.isEnabled(),
                    true, true, true,
                    authorities
            );
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/payments/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/offers",
                                "/coupon-codes",
                                "/feedback/list",
                                "/feedback/success",
                                "/user/packages",
                                "/payments",
                                "/payments/",
                                "/payments/receipt/**",
                                "/api/payments/**",
                                "/promotions/offers"

                        ).permitAll()

                        .requestMatchers(
                                "/coupons/**",
                                "/promotions/new",
                                "/promotions/save",
                                "/promotions/edit/**",
                                "/promotions/update/**",
                                "/promotions/delete/**",
                                "/promotions/countdown/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/admin/**",
                                "/feedback/admin",
                                "/feedback/approve/**",
                                "/feedback/delete/**",
                                "/booking/admin",
                                "/booking/admin/**"
                        ).hasRole("ADMIN")

                        .requestMatchers("/photographer/**").hasRole("PHOTOGRAPHER")

                        .requestMatchers(
                                "/user/cart",
                                "/user/cart/**",
                                "/feedback/check",
                                "/feedback/add",
                                "/feedback/save",
                                "/booking/new",
                                "/booking/create",
                                "/booking/cancel/**",
                                "/booking/track",
                                "/booking/track/**"
                        ).hasRole("CUSTOMER")

                        // ✅ FIX THIS LINE SEPARATELY
                        .requestMatchers("/booking/summary/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(successHandler)

                        // 🔥 ADD THIS LINE (MOST IMPORTANT)
                        .failureUrl("/auth/login?error=true&role=customer&view=customer")

                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {

                            String redirectUrl = "/auth/login?view=admin";

                            if (authentication != null) {
                                boolean isCustomer = authentication.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

                                if (isCustomer) {
                                    redirectUrl = "/auth/login?role=customer";
                                }
                            }

                            response.sendRedirect(redirectUrl + "&logout");
                        })
                );

        return http.build();
    }
}