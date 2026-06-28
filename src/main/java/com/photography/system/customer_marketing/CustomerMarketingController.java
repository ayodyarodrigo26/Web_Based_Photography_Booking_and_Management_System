package com.photography.system.customer_marketing;

import com.photography.system.marketing_management.coupon.CouponRepo;
import com.photography.system.marketing_management.promotion.Promotion;
import com.photography.system.marketing_management.promotion.PromotionRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CustomerMarketingController {

    private final CouponRepo couponRepo;
    private final PromotionRepo promotionRepo;

    public CustomerMarketingController(CouponRepo couponRepo,
                                       PromotionRepo promotionRepo) {
        this.couponRepo = couponRepo;
        this.promotionRepo = promotionRepo;
    }

    @GetMapping("/offers")
    public String offers(Model model) {

        List<Promotion> activePromotions = promotionRepo.findByActiveTrue();

        LocalDateTime now = LocalDateTime.now();

        List<Promotion> visible = activePromotions.stream()
                .filter(p -> {
                    if (!p.isCountdown()) return true;
                    if (p.getCountdownEnd() == null) return true;
                    return p.getCountdownEnd().isAfter(now);
                })
                .collect(Collectors.toList());

        model.addAttribute("promotions", visible);

        return "customer_marketing/offers";
    }

    @GetMapping("/coupon-codes")
    public String couponCodes(Model model) {
        model.addAttribute("coupons", couponRepo.findByActiveTrue());
        return "customer_marketing/coupon-codes";
    }
}