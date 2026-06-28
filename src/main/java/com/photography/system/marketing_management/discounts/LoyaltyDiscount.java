package com.photography.system.marketing_management.discounts;

import jakarta.persistence.*;

@Entity
public class LoyaltyDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double percentage;

    public Long getId() {
        return id;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}