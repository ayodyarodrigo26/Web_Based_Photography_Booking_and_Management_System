package com.photography.system.package_management.controller;

import com.photography.system.package_management.model.PackageCategory;
import com.photography.system.package_management.model.PhotographyPackage;
import com.photography.system.package_management.service.PackageCategoryService;
import com.photography.system.package_management.service.PhotographyPackageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class PackageCategoryController {

    private final PackageCategoryService categoryService;
    private final PhotographyPackageService packageService;

    public PackageCategoryController(PackageCategoryService categoryService,
                                     PhotographyPackageService packageService) {
        this.categoryService = categoryService;
        this.packageService = packageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "package_management/admin/category-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new PackageCategory());
        return "package_management/admin/category-create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("category") PackageCategory category,
                         BindingResult br,
                         Model model) {

        if (br.hasErrors()) {
            System.out.println("CATEGORY CREATE VALIDATION FAILED");
            br.getAllErrors().forEach(System.out::println);
            return "package_management/admin/category-create";
        }

        categoryService.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "package_management/admin/category-edit";
    }

    @PostMapping("/edit")
    public String update(@Valid @ModelAttribute("category") PackageCategory category,
                         BindingResult br,
                         Model model) {

        if (br.hasErrors()) {
            System.out.println("CATEGORY EDIT VALIDATION FAILED");
            br.getAllErrors().forEach(System.out::println);
            return "package_management/admin/category-edit";
        }

        categoryService.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/packages")
    public String viewPackages(@PathVariable Long id, Model model) {
        PackageCategory category = categoryService.findById(id);

        if (category == null) {
            return "redirect:/admin/categories";
        }

        List<PhotographyPackage> packages = packageService.findByCategory(category.getName());

        model.addAttribute("category", category);
        model.addAttribute("packages", packages);
        return "package_management/admin/category-packages";
    }
}