package com.photography.system.marketing_management.promotion;

import com.photography.system.package_management.service.PackageCategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/promotions")
public class PromotionController {

    private final PromotionRepo promotionRepo;
    private final PackageCategoryService packageCategoryService;

    public PromotionController(PromotionRepo promotionRepo,
                               PackageCategoryService packageCategoryService) {
        this.promotionRepo = promotionRepo;
        this.packageCategoryService = packageCategoryService;
    }

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword, Model model) {

        List<Promotion> promotions;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim();
            promotions = promotionRepo
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrApplicableCategoryContainingIgnoreCase(
                            searchKeyword, searchKeyword, searchKeyword
                    );
        } else {
            promotions = promotionRepo.findAll();
        }

        boolean changed = false;

        for (Promotion p : promotions) {
            if (p.isCountdown() && p.getCountdownEnd() != null && p.getCountdownEnd().isBefore(LocalDateTime.now())) {
                p.setCountdown(false);
                p.setCountdownEnd(null);
                changed = true;
            }
        }

        if (changed) {
            promotionRepo.saveAll(promotions);
        }

        model.addAttribute("promotions", promotions);
        model.addAttribute("keyword", keyword == null ? "" : keyword);

        return "marketing_management/promotion-list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        Promotion promotion = new Promotion();
        promotion.setActive(true);
        promotion.setCountdown(false);

        model.addAttribute("promotion", promotion);
        model.addAttribute("categories", packageCategoryService.findAll());
        return "marketing_management/promotion-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("promotion") Promotion promotion,
                       BindingResult bindingResult,
                       Model model,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       RedirectAttributes redirectAttributes)
            throws IOException {

        validateCountdown(promotion, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", packageCategoryService.findAll());
            return "marketing_management/promotion-form";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
            File folder = new File(uploadDir);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String original = imageFile.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + ext;
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            promotion.setImageName(fileName);
        }

        promotionRepo.save(promotion);
        redirectAttributes.addFlashAttribute("success", "Promotion added successfully!");
        return "redirect:/promotions";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<Promotion> promo = promotionRepo.findById(id);
        if (promo.isEmpty()) {
            return "redirect:/promotions";
        }

        model.addAttribute("promotion", promo.get());
        model.addAttribute("categories", packageCategoryService.findAll());
        return "marketing_management/promotion-edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("promotion") Promotion formPromotion,
                         BindingResult bindingResult,
                         Model model,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         RedirectAttributes redirectAttributes)
            throws IOException {

        validateCountdown(formPromotion, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", packageCategoryService.findAll());
            return "marketing_management/promotion-edit";
        }

        Promotion existing = promotionRepo.findById(id).orElse(null);
        if (existing == null) {
            return "redirect:/promotions";
        }

        existing.setTitle(formPromotion.getTitle());
        existing.setDescription(formPromotion.getDescription());
        existing.setDiscountPercent(formPromotion.getDiscountPercent());
        existing.setApplicableCategory(formPromotion.getApplicableCategory());
        existing.setActive(formPromotion.isActive());

        existing.setCountdown(formPromotion.isCountdown());
        if (formPromotion.isCountdown()) {
            existing.setCountdownEnd(formPromotion.getCountdownEnd());
        } else {
            existing.setCountdownEnd(null);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
            File folder = new File(uploadDir);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String original = imageFile.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + ext;
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            if (existing.getImageName() != null) {
                try {
                    Files.deleteIfExists(Paths.get(uploadDir, existing.getImageName()));
                } catch (Exception ignored) {
                }
            }

            existing.setImageName(fileName);
        }

        promotionRepo.save(existing);

        redirectAttributes.addFlashAttribute("success", "Promotion updated successfully!");

        return "redirect:/promotions";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Promotion existing = promotionRepo.findById(id).orElse(null);
        if (existing != null) {
            if (existing.getImageName() != null) {
                String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
                try {
                    Files.deleteIfExists(Paths.get(uploadDir, existing.getImageName()));
                } catch (Exception ignored) {
                }
            }
            promotionRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Promotion deleted successfully!");
        }
        return "redirect:/promotions";
    }

    @GetMapping("/countdown/{id}")
    public String countdownForm(@PathVariable Long id, Model model) {
        Promotion promo = promotionRepo.findById(id).orElse(null);
        if (promo == null) {
            return "redirect:/promotions";
        }

        model.addAttribute("promotion", promo);
        return "marketing_management/promotion-countdown";
    }

    @PostMapping("/countdown/{id}")
    public String saveCountdown(@PathVariable Long id,
                                @RequestParam(name = "countdown", required = false) String countdown,
                                @RequestParam(name = "countdownEnd", required = false) String countdownEndStr) {

        Promotion promo = promotionRepo.findById(id).orElse(null);
        if (promo == null) {
            return "redirect:/promotions";
        }

        boolean enable = (countdown != null);
        promo.setCountdown(enable);

        if (!enable) {
            promo.setCountdownEnd(null);
        } else {
            if (countdownEndStr != null && !countdownEndStr.isBlank()) {
                LocalDateTime parsed = LocalDateTime.parse(countdownEndStr);

                if (parsed.isBefore(LocalDateTime.now())) {
                    promo.setCountdownEnd(null);
                    promo.setCountdown(false);
                } else {
                    promo.setCountdownEnd(parsed);
                }
            } else {
                promo.setCountdownEnd(null);
                promo.setCountdown(false);
            }
        }

        promotionRepo.save(promo);
        return "redirect:/promotions";
    }

    private void validateCountdown(Promotion promotion, BindingResult bindingResult) {
        if (promotion.isCountdown()) {
            if (promotion.getCountdownEnd() == null) {
                bindingResult.rejectValue(
                        "countdownEnd",
                        "countdownEnd.empty",
                        "Please select a countdown end date & time"
                );
            } else if (promotion.getCountdownEnd().isBefore(LocalDateTime.now())) {
                bindingResult.rejectValue(
                        "countdownEnd",
                        "countdownEnd.past",
                        "Countdown end must be a future date & time"
                );
            }
        } else {
            promotion.setCountdownEnd(null);
        }
    }

    @GetMapping("/offers")
    public String getOffers(Model model) {

        List<Promotion> promotions = promotionRepo.findAll();

        model.addAttribute("promotions", promotions);
        model.addAttribute("selectedCategory", null);

        return "offers";
    }




}