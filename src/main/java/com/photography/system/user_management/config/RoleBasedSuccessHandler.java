package com.photography.system.user_management.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String next = request.getParameter("next");

        HttpSession session = request.getSession(false);
        if ((next == null || next.isBlank()) && session != null) {
            Object savedNext = session.getAttribute("LOGIN_NEXT_URL");
            if (savedNext != null) {
                next = savedNext.toString();
            }
        }

        // extra safety: only allow internal redirects
        if (next != null && !next.isBlank() && next.startsWith("/")) {
            if (session != null) {
                session.removeAttribute("LOGIN_NEXT_URL");
            }
            response.sendRedirect(next);
            return;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isPhotographer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PHOTOGRAPHER"));

        boolean isCustomer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        if (isAdmin) {
            response.sendRedirect("/admin/management-dashboard");
        } else if (isPhotographer) {
            response.sendRedirect("/photographer/dashboard");
        } else if (isCustomer) {
            response.sendRedirect("/user/dashboard");
        } else {
            response.sendRedirect("/");
        }
    }
}