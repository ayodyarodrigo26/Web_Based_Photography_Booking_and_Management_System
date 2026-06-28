package com.photography.system.user_management.service;

import com.photography.system.user_management.dto.RegisterAdminDto;
import com.photography.system.user_management.dto.RegisterCustomerDto;
import com.photography.system.user_management.dto.RegisterPhotographerDto;
import com.photography.system.user_management.dto.UpdateProfileDto;
import com.photography.system.user_management.model.Role;
import com.photography.system.user_management.model.User;
import com.photography.system.user_management.repository.RoleRepository;
import com.photography.system.user_management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepo,
                           RoleRepository roleRepo,
                           PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerCustomer(RegisterCustomerDto dto) {
        registerCustomerOrPhotographer(dto.getFirstName(), dto.getLastName(),
                dto.getEmail(), dto.getPhone(), dto.getPassword(), "ROLE_CUSTOMER");
    }

    @Override
    public void registerPhotographer(RegisterPhotographerDto dto) {
        registerCustomerOrPhotographer(dto.getFirstName(), dto.getLastName(),
                dto.getEmail(), dto.getPhone(), dto.getPassword(), "ROLE_PHOTOGRAPHER");
    }

    // ✅ NEW: Admin registration
    @Override
    public void registerAdmin(RegisterAdminDto dto) {
        registerCustomerOrPhotographer(dto.getFirstName(), dto.getLastName(),
                dto.getEmail(), dto.getPhone(), dto.getPassword(), "ROLE_ADMIN");
    }

    private void registerCustomerOrPhotographer(String firstName,
                                                String lastName,
                                                String email,
                                                String phone,
                                                String rawPassword,
                                                String roleName) {

        if (userRepo.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        Role role = roleRepo.findByName(roleName)
                .orElseGet(() -> roleRepo.save(new Role(roleName)));

        User user = new User();
        user.setFullName(firstName + " " + lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);

        userRepo.save(user);
    }

    @Override
    public User getByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public void updateProfile(String email, UpdateProfileDto dto) {
        User user = getByEmail(email);
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        // no need to call save() because @Transactional updates automatically
    }

    @Override
    public void deleteOwnAccount(String email) {
        User user = getByEmail(email);
        userRepo.delete(user);
    }
}