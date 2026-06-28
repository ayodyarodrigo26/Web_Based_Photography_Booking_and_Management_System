package com.photography.system.marketing_management.coupon;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/coupons")
public class CouponController {

    private final CouponRepo couponRepo;

    public CouponController(CouponRepo couponRepo) {
        this.couponRepo = couponRepo;
    }

    // ================= LIST =================
    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword, Model model) {

        List<Coupon> coupons;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim();

            Coupon.DiscountType type = null;
            if ("PERCENT".equalsIgnoreCase(searchKeyword)) {
                type = Coupon.DiscountType.PERCENT;
            } else if ("FIXED".equalsIgnoreCase(searchKeyword)) {
                type = Coupon.DiscountType.FIXED;
            }

            if (type != null) {
                coupons = couponRepo.findByCodeContainingIgnoreCaseOrDiscountType(searchKeyword, type);
            } else {
                coupons = couponRepo.findByCodeContainingIgnoreCaseOrDiscountType(searchKeyword, Coupon.DiscountType.PERCENT)
                        .stream()
                        .filter(c -> c.getCode() != null && c.getCode().toLowerCase().contains(searchKeyword.toLowerCase()))
                        .toList();
            }
        } else {
            coupons = couponRepo.findAll();
        }

        model.addAttribute("coupons", coupons);
        model.addAttribute("keyword", keyword == null ? "" : keyword);

        return "marketing_management/coupon-list";
    }

    // ================= NEW =================
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("types", Coupon.DiscountType.values());
        return "marketing_management/coupon-form";
    }

    // ================= SAVE =================
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("coupon") Coupon coupon,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        model.addAttribute("types", Coupon.DiscountType.values());

        if (coupon.getCode() != null) {
            coupon.setCode(coupon.getCode().trim().toUpperCase());
        }

        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENT
                && coupon.getValue() != null
                && coupon.getValue() > 100) {
            bindingResult.rejectValue("value", "invalid", "Percentage discount cannot exceed 100");
        }

        if (bindingResult.hasErrors()) {
            return "marketing_management/coupon-form";
        }

        if (couponRepo.existsByCode(coupon.getCode())) {
            bindingResult.rejectValue("code", "duplicate", "Coupon code already exists");
            return "marketing_management/coupon-form";
        }

        couponRepo.save(coupon);

        redirectAttributes.addFlashAttribute("success", "Coupon created successfully!");
        return "redirect:/coupons";
    }

    // ================= EDIT =================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Coupon coupon = couponRepo.findById(id).orElse(null);
        if (coupon == null) return "redirect:/coupons";

        model.addAttribute("coupon", coupon);
        model.addAttribute("types", Coupon.DiscountType.values());
        return "marketing_management/coupon-edit";
    }

    // ================= UPDATE =================
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("coupon") Coupon formCoupon,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        model.addAttribute("types", Coupon.DiscountType.values());

        if (formCoupon.getCode() != null) {
            formCoupon.setCode(formCoupon.getCode().trim().toUpperCase());
        }

        if (bindingResult.hasErrors()) {
            return "marketing_management/coupon-edit";
        }

        if (couponRepo.existsByCodeAndIdNot(formCoupon.getCode(), id)) {
            bindingResult.rejectValue("code", "duplicate", "Coupon code already exists");
            return "marketing_management/coupon-edit";
        }

        Coupon existing = couponRepo.findById(id).orElse(null);
        if (existing == null) return "redirect:/coupons";

        existing.setCode(formCoupon.getCode());
        existing.setDiscountType(formCoupon.getDiscountType());
        existing.setValue(formCoupon.getValue());
        existing.setActive(formCoupon.isActive());

        couponRepo.save(existing);

        redirectAttributes.addFlashAttribute("success", "Coupon updated successfully!");
        return "redirect:/coupons";
    }

    // ================= DELETE =================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {

        couponRepo.deleteById(id);

        redirectAttributes.addFlashAttribute("error", "Coupon deleted successfully!");
        return "redirect:/coupons";
    }
}