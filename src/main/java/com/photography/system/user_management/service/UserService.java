package com.photography.system.user_management.service;

import com.photography.system.user_management.dto.RegisterAdminDto;
import com.photography.system.user_management.dto.RegisterCustomerDto;
import com.photography.system.user_management.dto.RegisterPhotographerDto;
import com.photography.system.user_management.dto.UpdateProfileDto;
import com.photography.system.user_management.model.User;

public interface UserService {

    void registerCustomer(RegisterCustomerDto dto);

    void registerPhotographer(RegisterPhotographerDto dto);

    //  NEW: Admin registration
    void registerAdmin(RegisterAdminDto dto);

    User getByEmail(String email);

    void updateProfile(String email, UpdateProfileDto dto);

    void deleteOwnAccount(String email);
}