package com.photography.system.user_management.controller;

import com.photography.system.user_management.dto.RegisterAdminDto;
import com.photography.system.user_management.model.User;
import com.photography.system.user_management.repository.UserRepository;
import com.photography.system.user_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.photography.system.booking_management.repository.BookingRepository;
import java.util.Map;
import java.util.HashMap;

import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.photography.system.user_management.dto.UpdateProfileDto;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    public AdminController(UserRepository userRepository,
                           UserService userService,
                           BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/users")
    public String usersManagement(@RequestParam(value = "role", required = false, defaultValue = "ALL") String role,
                                  Model model) {

        List<User> users = userRepository.findAll();

        if (!"ALL".equalsIgnoreCase(role)) {
            users = users.stream()
                    .filter(user -> user.getRole() != null
                            && user.getRole().getName() != null
                            && user.getRole().getName().equalsIgnoreCase("ROLE_" + role))
                    .toList();
        }

        Map<Long, Boolean> loyaltyMap = new HashMap<>();

        for (User user : users) {
            boolean isLoyal = false;

            if (user.getRole() != null &&
                    "ROLE_CUSTOMER".equalsIgnoreCase(user.getRole().getName())) {

                long paidCount = bookingRepository.countPaidBookingsByEmail(user.getEmail());
                isLoyal = paidCount >= 3;
            }

            loyaltyMap.put(user.getId(), isLoyal);
        }

        model.addAttribute("users", users);
        model.addAttribute("loyaltyMap", loyaltyMap);
        model.addAttribute("selectedRole", role.toUpperCase());

        return "user_management/admin/dashboard";
    }

    @GetMapping("/register")
    public String adminRegisterForm(Model model) {
        model.addAttribute("form", new RegisterAdminDto());
        return "user_management/admin/register-admin";
    }

    @PostMapping("/register")
    public String registerAdmin(@Valid @ModelAttribute("form") RegisterAdminDto form,
                                BindingResult br,
                                Model model) {

        if (br.hasErrors()) {
            return "user_management/admin/register-admin";
        }

        if (userRepository.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "Email already exists");
            return "user_management/admin/register-admin";
        }

        try {
            userService.registerAdmin(form);
            return "redirect:/admin/users?adminCreated";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "user_management/admin/register-admin";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes ra) {

        try {
            User currentUser = userService.getByEmail(auth.getName());

            // ❌ prevent deleting yourself
            if (currentUser.getId().equals(id)) {
                ra.addFlashAttribute("error", "You cannot delete your own account");
                return "redirect:/admin/users";
            }

            userRepository.deleteById(id);

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Cannot delete user (linked data exists)");
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam(required = false) String phone,
                                Authentication authentication,
                                RedirectAttributes ra) {

        String email = authentication.getName();

        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setFullName(name);
        dto.setPhone(phone);

        userService.updateProfile(email, dto);
        ra.addFlashAttribute("success", "Profile updated successfully!");

        return "redirect:/admin/users";
    }
}