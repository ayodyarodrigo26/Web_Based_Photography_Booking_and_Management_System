package com.photography.system.package_management.controller;

import com.photography.system.package_management.model.PhotographyPackage;
import com.photography.system.package_management.service.PackageCategoryService;
import com.photography.system.package_management.service.PhotographyPackageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/packages")
public class PhotographyPackageController {

    private final PhotographyPackageService packageService;
    private final PackageCategoryService categoryService;

    public PhotographyPackageController(PhotographyPackageService packageService,
                                        PackageCategoryService categoryService) {
        this.packageService = packageService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("packages", packageService.findAll());
        return "package_management/admin/package-list";
    }

    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) String category, Model model) {
        PhotographyPackage pkg = new PhotographyPackage();

        if (category != null && !category.isBlank()) {
            pkg.setCategory(category.trim());
        }

        model.addAttribute("pkg", pkg);
        model.addAttribute("categories", categoryService.findAll());
        return "package_management/admin/package-create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("pkg") PhotographyPackage pkg,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "package_management/admin/package-create";
        }

        packageService.save(pkg);
        ra.addFlashAttribute("success", "Package created successfully.");
        return "redirect:/admin/packages";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        PhotographyPackage pkg = packageService.findById(id);

        if (pkg == null) {
            return "redirect:/admin/packages";
        }

        model.addAttribute("pkg", pkg);
        model.addAttribute("categories", categoryService.findAll());
        return "package_management/admin/package-edit";
    }

    @PostMapping("/edit")
    public String update(@Valid @ModelAttribute("pkg") PhotographyPackage pkg,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "package_management/admin/package-edit";
        }

        packageService.save(pkg);
        ra.addFlashAttribute("success", "Package updated successfully.");
        return "redirect:/admin/packages";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        packageService.deleteById(id);
        ra.addFlashAttribute("success", "Package deleted successfully.");
        return "redirect:/admin/packages";
    }
}