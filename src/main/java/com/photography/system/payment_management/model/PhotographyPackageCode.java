package com.photography.system.payment_management.model;

import java.math.BigDecimal;

/**
 * Fixed catalog packages with list prices
 * (Epic 4 pricing builds on these + date rules + promos).
 */
public enum PhotographyPackageCode {
    STANDARD("STANDARD", "Standard Package", new BigDecimal("50000")),
    PREMIUM("PREMIUM", "Premium Package", new BigDecimal("125000")),
    WEDDING("WEDDING", "Wedding Deluxe", new BigDecimal("250000"));

    private final String code;
    private final String displayName;
    private final BigDecimal listPrice;

    PhotographyPackageCode(String code, String displayName, BigDecimal listPrice) {
        this.code = code;
        this.displayName = displayName;
        this.listPrice = listPrice;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getListPrice() {
        return listPrice;
    }

    public static PhotographyPackageCode fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return STANDARD;
        }
        String u = raw.trim().toUpperCase();
        for (PhotographyPackageCode p : values()) {
            if (p.code.equals(u)) {
                return p;
            }
        }
        return STANDARD;
    }
}