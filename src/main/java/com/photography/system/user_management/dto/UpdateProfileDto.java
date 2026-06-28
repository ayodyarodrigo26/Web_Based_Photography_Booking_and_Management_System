package com.photography.system.user_management.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateProfileDto {

    @NotBlank
    private String fullName;

    private String phone;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}