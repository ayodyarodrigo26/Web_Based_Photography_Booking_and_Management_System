package com.photography.system.marketing_management.promotion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionRepo extends JpaRepository<Promotion, Long> {

    List<Promotion> findByActiveTrue();

    List<Promotion> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrApplicableCategoryContainingIgnoreCase(
            String titleKeyword,
            String descriptionKeyword,
            String categoryKeyword
    );

    List<Promotion> findByApplicableCategoryIgnoreCase(String category);

    @Query("SELECT p FROM Promotion p WHERE LOWER(p.applicableCategory) = LOWER(:category)")
    List<Promotion> findByCategoryClean(@Param("category") String category);
}