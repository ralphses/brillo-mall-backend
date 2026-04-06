package com.clickstechnology.Brillo.Mall.domain.business;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

interface BusinessRepository extends JpaRepository<Business, Long> {
    boolean existsByName(String name);
    Optional<Business> findByName(String name);
    Optional<Business> findByReference(String businessId);
    Optional<Business> findBySlug(String slug);
    boolean existsByReference(String businessId);
    Page<Business> findAllByOwnerId(String ownerId, Pageable pageable);
    List<Business> findAllByReferenceIn(Set<String> businessIds);
    List<Business> findAllByOwnerId(String ownerId);
}