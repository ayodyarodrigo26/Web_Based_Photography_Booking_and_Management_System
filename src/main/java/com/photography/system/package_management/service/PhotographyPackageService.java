package com.photography.system.package_management.service;

import com.photography.system.package_management.model.PhotographyPackage;
import com.photography.system.package_management.repository.PhotographyPackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhotographyPackageService {

    private final PhotographyPackageRepository repo;

    public PhotographyPackageService(PhotographyPackageRepository repo) {
        this.repo = repo;
    }

    public PhotographyPackage save(PhotographyPackage pkg) {
        return repo.save(pkg);
    }

    public List<PhotographyPackage> findAll() {
        return repo.findAll();
    }

    public PhotographyPackage findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public List<PhotographyPackage> findByCategory(String category) {
        return repo.findByCategoryNormalized(category);
    }
}