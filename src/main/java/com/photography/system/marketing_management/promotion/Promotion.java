package com.photography.system.marketing_management.promotion;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Discount percentage is required")
    @Min(value = 1, message = "Discount percent must be at least 1")
    @Max(value = 100, message = "Discount percent cannot exceed 100")
    private Integer discountPercent;

    private String imageName;

    private boolean active = true;

    private boolean countdown = false;

    private LocalDateTime countdownEnd;

    @NotBlank(message = "Applicable package category is required")
    @Column(length = 100)
    private String applicableCategory;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCountdown() {
        return countdown;
    }

    public void setCountdown(boolean countdown) {
        this.countdown = countdown;
    }

    public LocalDateTime getCountdownEnd() {
        return countdownEnd;
    }

    public void setCountdownEnd(LocalDateTime countdownEnd) {
        this.countdownEnd = countdownEnd;
    }

    public String getApplicableCategory() {
        return applicableCategory;
    }

    public void setApplicableCategory(String applicableCategory) {
        this.applicableCategory = applicableCategory != null
                ? applicableCategory.trim().toUpperCase()
                : null;
    }

    public boolean isCountdownExpired() {
        return countdown && countdownEnd != null && countdownEnd.isBefore(LocalDateTime.now());
    }

    public String getCountdownState() {
        if (!countdown) return "OFF";
        if (countdownEnd == null) return "ON";
        return isCountdownExpired() ? "EXPIRED" : "ON";
    }


}