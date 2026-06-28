package com.photography.system.booking_management.repository;

import com.photography.system.booking_management.entity.Photographer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotographerRepository extends JpaRepository<Photographer, Long> {
}