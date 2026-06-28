package com.photography.system.package_management.repository;

import com.photography.system.package_management.model.PhotographyPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotographyPackageRepository extends JpaRepository<PhotographyPackage, Long> {

    @Query("SELECT p FROM PhotographyPackage p " +
            "WHERE LOWER(TRIM(p.category)) = LOWER(TRIM(:category))")
    List<PhotographyPackage> findByCategoryNormalized(@Param("category") String category);
}