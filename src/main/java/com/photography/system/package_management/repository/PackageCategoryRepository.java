package com.photography.system.package_management.repository;

import com.photography.system.package_management.model.PackageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageCategoryRepository extends JpaRepository<PackageCategory, Long> {
}