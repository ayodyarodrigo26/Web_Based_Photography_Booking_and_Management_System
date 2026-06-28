package com.photography.system.user_management.security;

import java.util.Set;

import static com.photography.system.user_management.security.Permission.*;

public class RolePermissions {

    public static Set<Permission> forRole(String roleName) {
        if (roleName == null) return Set.of();

        return switch (roleName) {
            case "ROLE_ADMIN" -> Set.of(
                    ADMIN_READ, ADMIN_WRITE,
                    USER_READ, USER_WRITE,
                    PHOTOGRAPHER_READ, PHOTOGRAPHER_WRITE,
                    ACCOUNT_READ, ACCOUNT_WRITE
            );
            case "ROLE_PHOTOGRAPHER" -> Set.of(
                    PHOTOGRAPHER_READ, PHOTOGRAPHER_WRITE,
                    ACCOUNT_READ, ACCOUNT_WRITE
            );
            default -> Set.of( // CUSTOMER
                    USER_READ, USER_WRITE,
                    ACCOUNT_READ, ACCOUNT_WRITE
            );
        };
    }

    private RolePermissions() {}
}