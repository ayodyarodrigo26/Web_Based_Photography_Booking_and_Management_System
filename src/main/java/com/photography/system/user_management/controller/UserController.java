package com.photography.system.user_management.controller;

import com.photography.system.user_management.dto.UpdateProfileDto;
import com.photography.system.user_management.model.User;
import com.photography.system.user_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        User user = userService.getByEmail(email);

        model.addAttribute("user", user);
        return "user_management/user/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        String email = auth.getName();
        User user = userService.getByEmail(email);

        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());

        model.addAttribute("user", user);
        model.addAttribute("form", dto);
        return "user_management/user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            Authentication auth,
            @Valid @ModelAttribute("form") UpdateProfileDto form,
            BindingResult br,
            Model model
    ) {
        if (br.hasErrors()) {
            model.addAttribute("user", userService.getByEmail(auth.getName()));
            return "user_management/user/profile";
        }

        userService.updateProfile(auth.getName(), form);
        return "redirect:/user/dashboard";
    }


    @GetMapping("/delete")
    public String confirmDelete() {
        return "user_management/user/confirm-delete";
    }

    @PostMapping("/delete")
    public String deleteAccount(Authentication auth) {

        userService.deleteOwnAccount(auth.getName());

        SecurityContextHolder.clearContext();

        return "redirect:/";
    }
}