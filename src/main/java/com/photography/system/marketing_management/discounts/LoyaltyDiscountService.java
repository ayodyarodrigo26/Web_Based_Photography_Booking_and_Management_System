package com.photography.system.marketing_management.discounts;

import org.springframework.stereotype.Service;

@Service
public class LoyaltyDiscountService {

    private final LoyaltyDiscountRepository repo;

    public LoyaltyDiscountService(LoyaltyDiscountRepository repo) {
        this.repo = repo;
    }

    // GET CURRENT DISCOUNT (from DB)
    public LoyaltyDiscount getDiscount() {
        return repo.findAll().stream().findFirst().orElse(null);
    }

    // GET PERCENTAGE ONLY
    public double getDiscountPercentage() {
        LoyaltyDiscount discount = getDiscount();
        return (discount != null) ? discount.getPercentage() : 0;
    }

    // UPDATE VALUE
    public void update(double percentage) {
        LoyaltyDiscount d = getDiscount();

        if (d == null) {
            d = new LoyaltyDiscount();
        }

        d.setPercentage(percentage);
        repo.save(d);
    }


}