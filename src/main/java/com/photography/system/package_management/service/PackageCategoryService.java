package com.photography.system.package_management.service;

import com.photography.system.package_management.model.PackageCategory;
import com.photography.system.package_management.repository.PackageCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackageCategoryService {

    private final PackageCategoryRepository repo;

    public PackageCategoryService(PackageCategoryRepository repo) {
        this.repo = repo;
    }

    public List<PackageCategory> findAll() {
        return repo.findAll();
    }

    public PackageCategory findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public PackageCategory save(PackageCategory category) {
        return repo.save(category);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}