package com.photography.system.user_management.controller;

import com.photography.system.user_management.dto.RegisterCustomerDto;
import com.photography.system.user_management.dto.RegisterPhotographerDto;
import com.photography.system.user_management.repository.UserRepository;
import com.photography.system.user_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.photography.system.user_management.model.User;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "role", required = false) String role, // ✅ ADD BACK
            @RequestParam(value = "next", required = false) String next,
            @RequestParam(value = "view", required = false) String view,
            Model model,
            HttpSession session) {

        // handle next
        if (next != null && !next.isBlank()) {
            session.setAttribute("LOGIN_NEXT_URL", next);
        }

        Object savedNext = session.getAttribute("LOGIN_NEXT_URL");
        if (savedNext != null) {
            model.addAttribute("next", savedNext.toString());
        }

        // ✅ FIX: handle role again
        String selectedRole = null;
        if (role != null && !role.isBlank()) {
            selectedRole = role.trim().toUpperCase();
        }

        model.addAttribute("selectedRole", selectedRole);

        // view handling
        if (view == null || view.isBlank()) {
            view = "customer";
        }
        model.addAttribute("view", view);

        return "user_management/auth/login";
    }

    @GetMapping("/register/customer")
    public String customerRegisterForm(Model model) {
        model.addAttribute("form", new RegisterCustomerDto());
        return "user_management/auth/register-customer";
    }

    @PostMapping("/register/customer")
    public String registerCustomer(@Valid @ModelAttribute("form") RegisterCustomerDto form,
                                   BindingResult br,
                                   Model model) {

        if (br.hasErrors()) {
            return "user_management/auth/register-customer";
        }

        if (userRepository.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "Email already exists");
            return "user_management/auth/register-customer";
        }

        try {
            userService.registerCustomer(form);
            return "redirect:/auth/login?registered";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "user_management/auth/register-customer";
        }
    }

    @GetMapping("/register/photographer")
    public String photographerRegisterForm(Model model) {
        model.addAttribute("form", new RegisterPhotographerDto());
        return "user_management/auth/register-photographer"; // ✅ FIXED
    }

    @PostMapping("/register/photographer")
    public String registerPhotographer(@Valid @ModelAttribute("form") RegisterPhotographerDto form,
                                       BindingResult br,
                                       Model model) {

        if (br.hasErrors()) {
            return "user_management/auth/register-photographer"; // ✅ FIXED
        }

        if (userRepository.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "Email already exists");
            return "user_management/auth/register-photographer";
        }

        try {
            userService.registerPhotographer(form);
            return "redirect:/auth/admin-login?registered"; // ✅ FIXED
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "user_management/auth/register-photographer";
        }
    }

    // ==============================
// FORGOT PASSWORD
// ==============================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "user_management/auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email,
                                       Model model,
                                       HttpSession session) {

        var userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "No account found with this email.");
            return "user_management/auth/forgot-password";
        }

        session.setAttribute("RESET_EMAIL", email);

        return "redirect:/auth/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session) {

        if (session.getAttribute("RESET_EMAIL") == null) {
            return "redirect:/auth/forgot-password";
        }

        return "user_management/auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam String password,
                                      HttpSession session,
                                      RedirectAttributes ra) {

        String email = (String) session.getAttribute("RESET_EMAIL");

        if (email == null) {
            return "redirect:/auth/forgot-password";
        }

        User user = userRepository.findByEmail(email).orElseThrow();

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        session.removeAttribute("RESET_EMAIL");

        return "redirect:/auth/login?resetSuccess";
    }

    @GetMapping("/admin-login")
    public String adminLoginPage(Model model, HttpSession session) {

        Object savedNext = session.getAttribute("LOGIN_NEXT_URL");
        if (savedNext != null) {
            model.addAttribute("next", savedNext.toString()); // ✅ FIXED
        }

        model.addAttribute("view", "admin");

        return "user_management/auth/login";
    }


}