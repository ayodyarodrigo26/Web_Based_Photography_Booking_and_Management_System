package com.photography.system.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManagementDashboardController {

    @GetMapping("/admin/management-dashboard")
    public String managementDashboard() {
        return "common/management-dashboard";
    }
}