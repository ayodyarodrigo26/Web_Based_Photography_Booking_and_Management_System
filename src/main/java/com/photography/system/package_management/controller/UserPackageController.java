package com.photography.system.package_management.controller;

import com.photography.system.booking_management.repository.BookingRepository;
import com.photography.system.marketing_management.coupon.Coupon;
import com.photography.system.marketing_management.coupon.CouponRepo;
import com.photography.system.marketing_management.promotion.Promotion;
import com.photography.system.marketing_management.promotion.PromotionRepo;
import com.photography.system.package_management.model.PhotographyPackage;
import com.photography.system.package_management.service.PackageCategoryService;
import com.photography.system.package_management.service.PhotographyPackageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.photography.system.marketing_management.discounts.LoyaltyDiscountService;


import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserPackageController {

    private final PhotographyPackageService service;
    private final PackageCategoryService categoryService;
    private final CouponRepo couponRepo;
    private final PromotionRepo promotionRepo;
    private final BookingRepository bookingRepository;
    private final LoyaltyDiscountService loyaltyDiscountService;

    private static final String CART_SESSION_KEY = "CART";
    private static final String CART_ADDONS_SESSION_KEY = "CART_ADDONS";
    private static final String APPLIED_COUPON_SESSION_KEY = "APPLIED_COUPON";

    private static final Map<String, Double> AVAILABLE_ADDONS = new LinkedHashMap<>();

    static {
        AVAILABLE_ADDONS.put("Large Frame (16x20)", 8000.0);
        AVAILABLE_ADDONS.put("Medium Frame (12x18)", 5000.0);
        AVAILABLE_ADDONS.put("Canvas Print", 10000.0);
        AVAILABLE_ADDONS.put("Premium Album", 15000.0);
        AVAILABLE_ADDONS.put("Extra Coverage Hour", 7000.0);
        AVAILABLE_ADDONS.put("Drone Coverage", 12000.0);
        AVAILABLE_ADDONS.put("Highlight Video", 18000.0);
        AVAILABLE_ADDONS.put("Extra 25 Edited Photos", 6000.0);
    }

    public UserPackageController(
            PhotographyPackageService service,
            PackageCategoryService categoryService,
            CouponRepo couponRepo,
            PromotionRepo promotionRepo,
            BookingRepository bookingRepository,
            LoyaltyDiscountService loyaltyDiscountService // ✅ ADD
    ) {
        this.service = service;
        this.categoryService = categoryService;
        this.couponRepo = couponRepo;
        this.promotionRepo = promotionRepo;
        this.bookingRepository = bookingRepository;
        this.loyaltyDiscountService = loyaltyDiscountService; // ✅ ADD
    }

    // ==============================
    // PACKAGES
    // ==============================
    @GetMapping("/packages")
    public String packages(@RequestParam(required = false) String category, Model model) {

        if (category != null) category = category.trim();

        if (category != null && !category.isEmpty()) {
            model.addAttribute("packages", service.findByCategory(category));
            model.addAttribute("selectedCategory", category);
        } else {
            model.addAttribute("packages", service.findAll());
            model.addAttribute("selectedCategory", "All");
        }

        model.addAttribute("categories", categoryService.findAll());
        return "package_management/user/packages";
    }

    // ==============================
    // SESSION HELPERS
    // ==============================
    @SuppressWarnings("unchecked")
    private Set<Long> getCart(HttpSession session) {
        Object obj = session.getAttribute(CART_SESSION_KEY);
        if (obj == null) {
            Set<Long> cart = new LinkedHashSet<>();
            session.setAttribute(CART_SESSION_KEY, cart);
            return cart;
        }
        return (Set<Long>) obj;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getSelectedAddOns(HttpSession session) {
        Object obj = session.getAttribute(CART_ADDONS_SESSION_KEY);
        if (obj == null) {
            Set<String> selected = new LinkedHashSet<>();
            session.setAttribute(CART_ADDONS_SESSION_KEY, selected);
            return selected;
        }
        return (Set<String>) obj;
    }

    private String getAppliedCouponCode(HttpSession session) {
        Object obj = session.getAttribute(APPLIED_COUPON_SESSION_KEY);
        return obj == null ? null : obj.toString();
    }

    // ==============================
    // CART
    // ==============================
    @GetMapping("/cart")
    public String viewCart(HttpSession session,
                           Model model,
                           Authentication authentication) {

        Set<Long> cart = getCart(session);
        Set<String> selectedAddOns = getSelectedAddOns(session);

        List<Map<String, Object>> items = new ArrayList<>();
        double packageTotal = 0.0;
        double promotionDiscount = 0.0;
        List<String> appliedPromotions = new ArrayList<>();

        // ===== PACKAGES =====
        for (Long id : cart) {
            PhotographyPackage pkg = service.findById(id);

            if (pkg != null) {
                double price = pkg.getPrice() != null ? pkg.getPrice() : 0.0;

                Promotion promo = promotionRepo.findByActiveTrue().stream()
                        .filter(p -> p.getApplicableCategory() != null &&
                                p.getApplicableCategory().equalsIgnoreCase(pkg.getCategory()))
                        .findFirst().orElse(null);

                if (promo != null) {
                    double discount = price * promo.getDiscountPercent() / 100.0;
                    promotionDiscount += discount;

                    if (!appliedPromotions.contains(promo.getTitle())) {
                        appliedPromotions.add(promo.getTitle());
                    }
                }

                // ✅ FIX HERE
                Map<String, Object> row = new HashMap<>();
                row.put("pkg", pkg);
                row.put("matchedPromotion", promo); // 🔥 REQUIRED
                items.add(row);

                packageTotal += price;
            }
        }

        // ===== ADDONS =====
        double addOnTotal = selectedAddOns.stream()
                .mapToDouble(a -> AVAILABLE_ADDONS.getOrDefault(a, 0.0))
                .sum();

        double subtotal = packageTotal + addOnTotal;

        // ===== COUPON =====
        double couponDiscount = 0.0;
        String code = getAppliedCouponCode(session);

        Coupon coupon = null;
        if (code != null) {
            coupon = couponRepo.findByCodeIgnoreCaseAndActiveTrue(code).orElse(null);
        }

        if (coupon != null) {
            double base = subtotal - promotionDiscount;

            if (coupon.getDiscountType() == Coupon.DiscountType.PERCENT) {
                couponDiscount = base * coupon.getValue() / 100.0;
            } else {
                couponDiscount = coupon.getValue();
            }
        }

        // ===== LOYALTY =====
        double loyaltyDiscount = 0.0;
        boolean isLoyalCustomer = false;
        double loyaltyPercentage = 0.0;

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String email = authentication.getName();

            long bookingCount = bookingRepository.countPaidBookingsByEmail(email);

            if (bookingCount >= 3) {
                isLoyalCustomer = true;

                double base = subtotal - promotionDiscount - couponDiscount;

                loyaltyPercentage = loyaltyDiscountService.getDiscountPercentage();

                loyaltyDiscount = base * (loyaltyPercentage / 100.0);
            }
        }

        double finalTotal = subtotal - promotionDiscount - couponDiscount - loyaltyDiscount;
        if (finalTotal < 0) finalTotal = 0;

        // ===== MODEL =====
        model.addAttribute("items", items);
        model.addAttribute("packageTotal", packageTotal);
        model.addAttribute("addOnTotal", addOnTotal);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("promotionDiscount", promotionDiscount);
        model.addAttribute("couponDiscount", couponDiscount);
        model.addAttribute("loyaltyDiscount", loyaltyDiscount);
        model.addAttribute("loyaltyPercentage", loyaltyPercentage);
        model.addAttribute("isLoyalCustomer", isLoyalCustomer);
        model.addAttribute("finalTotal", finalTotal);

        // 🔥 IMPORTANT (your HTML uses these)
        model.addAttribute("appliedPromotions", appliedPromotions);
        model.addAttribute("appliedCouponCode", code);
        model.addAttribute("selectedAddOns", selectedAddOns);
        model.addAttribute("availableAddOns", AVAILABLE_ADDONS);

        return "package_management/user/cart";
    }

    // ==============================
    // ADD TO CART
    // ==============================
    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session) {
        getCart(session).add(id);
        return "redirect:/user/cart";
    }

    @GetMapping("/cart/add/{id}")
    public String addToCartGet(@PathVariable Long id, HttpSession session) {
        return addToCart(id, session);
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        Set<Long> cart = getCart(session);

        cart.remove(id);

        session.setAttribute(CART_SESSION_KEY, cart);

        //  SUCCESS MESSAGE
        ra.addFlashAttribute("successMessage", "Item removed successfully");

        return "redirect:/user/cart";
    }

    @PostMapping("/cart/addons")
    public String applyAddOns(@RequestParam(value = "addons", required = false) List<String> addons,
                              HttpSession session) {

        Set<String> selectedAddOns = getSelectedAddOns(session);

        // clear previous selections
        selectedAddOns.clear();

        // add new selections
        if (addons != null) {
            selectedAddOns.addAll(addons);
        }

        // save back to session
        session.setAttribute(CART_ADDONS_SESSION_KEY, selectedAddOns);

        return "redirect:/user/cart";
    }

    @GetMapping("/cart/checkout")
    public String proceedToCheckout(HttpSession session, Authentication authentication) {

        Set<Long> cart = getCart(session);
        Set<String> selectedAddOns = getSelectedAddOns(session);

        List<Map<String, Object>> items = new ArrayList<>();

        double packageTotal = 0.0;
        double promotionDiscount = 0.0;
        List<String> appliedPromotions = new ArrayList<>();

        // ===== PACKAGES =====
        for (Long id : cart) {
            PhotographyPackage pkg = service.findById(id);

            if (pkg != null) {
                double price = pkg.getPrice() != null ? pkg.getPrice() : 0.0;

                Promotion promo = promotionRepo.findByActiveTrue().stream()
                        .filter(p -> p.getApplicableCategory() != null &&
                                p.getApplicableCategory().equalsIgnoreCase(pkg.getCategory()))
                        .findFirst().orElse(null);

                double lineDiscount = 0;

                if (promo != null) {
                    lineDiscount = price * promo.getDiscountPercent() / 100.0;
                    promotionDiscount += lineDiscount;

                    String label = promo.getTitle() + " (" + promo.getDiscountPercent() + "% off)";
                    if (!appliedPromotions.contains(label)) {
                        appliedPromotions.add(label);
                    }
                }

                Map<String, Object> row = new HashMap<>();
                row.put("pkg", pkg);
                items.add(row);

                packageTotal += price;
            }
        }

        // ===== ADDONS =====
        double addOnTotal = selectedAddOns.stream()
                .mapToDouble(a -> AVAILABLE_ADDONS.getOrDefault(a, 0.0))
                .sum();

        double subtotal = packageTotal + addOnTotal;

        // ===== COUPON =====
        String code = getAppliedCouponCode(session);
        Coupon coupon = null;
        double couponDiscount = 0.0;

        if (code != null) {
            coupon = couponRepo.findByCodeIgnoreCaseAndActiveTrue(code).orElse(null);

            if (coupon != null) {
                double base = subtotal - promotionDiscount;

                if (coupon.getDiscountType() == Coupon.DiscountType.PERCENT) {
                    couponDiscount = base * coupon.getValue() / 100.0;
                } else {
                    couponDiscount = coupon.getValue();
                }
            }
        }

        double loyaltyDiscount = 0.0;
        boolean isLoyalCustomer = false;
        double loyaltyPercentage = 0.0;

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String email = authentication.getName();

            long bookingCount = bookingRepository.countPaidBookingsByEmail(email);

            if (bookingCount >= 3) {
                isLoyalCustomer = true;

                double base = subtotal - promotionDiscount - couponDiscount;

                loyaltyPercentage = loyaltyDiscountService.getDiscountPercentage();

                loyaltyDiscount = base * (loyaltyPercentage / 100.0);
            }
        }

        double finalTotal = subtotal - promotionDiscount - couponDiscount - loyaltyDiscount;
        if (finalTotal < 0) finalTotal = 0;

        // ===== BUILD CORRECT SUMMARY =====
        Map<String, Object> summary = new HashMap<>();

        List<String> packageNames = new ArrayList<>();
        List<String> packageCategories = new ArrayList<>();

        for (Map<String, Object> row : items) {
            PhotographyPackage pkg = (PhotographyPackage) row.get("pkg");
            if (pkg != null) {
                packageNames.add(pkg.getName());
                packageCategories.add(pkg.getCategory());
            }
        }

        summary.put("packageNames", packageNames);
        summary.put("packageCategories", packageCategories);
        summary.put("selectedAddOns", new ArrayList<>(selectedAddOns));
        summary.put("appliedPromotions", appliedPromotions);
        summary.put("appliedCouponCode", code);

        summary.put("packageTotal", packageTotal);
        summary.put("addOnTotal", addOnTotal);
        summary.put("loyaltyDiscount", loyaltyDiscount);
        summary.put("promotionDiscount", promotionDiscount);
        summary.put("couponDiscount", couponDiscount);
        summary.put("finalTotal", finalTotal);

        // 🔥 SAVE CORRECT STRUCTURE
        session.setAttribute("RESERVATION_SUMMARY", summary);

        return "redirect:/booking/new";
    }

    @PostMapping("/cart/apply-coupon")
    public String applyCoupon(@RequestParam("couponCode") String couponCode,
                              HttpSession session,
                              RedirectAttributes ra) {

        if (couponCode == null || couponCode.trim().isEmpty()) {
            ra.addFlashAttribute("couponMessage", "Please enter a coupon code.");
            return "redirect:/user/cart";
        }

        Optional<Coupon> couponOpt = couponRepo.findByCodeIgnoreCaseAndActiveTrue(couponCode.trim());

        if (couponOpt.isEmpty()) {
            ra.addFlashAttribute("couponMessage", "Invalid or inactive coupon.");
            return "redirect:/user/cart";
        }

        //  SAVE TO SESSION
        session.setAttribute(APPLIED_COUPON_SESSION_KEY, couponOpt.get().getCode());

        ra.addFlashAttribute("couponMessage", "Coupon applied successfully!");
        return "redirect:/user/cart";
    }

    @GetMapping("/cart/remove-coupon")
    public String removeCoupon(HttpSession session, RedirectAttributes ra) {
        session.removeAttribute(APPLIED_COUPON_SESSION_KEY);
        ra.addFlashAttribute("couponMessage", "Coupon removed.");
        return "redirect:/user/cart";
    }
}