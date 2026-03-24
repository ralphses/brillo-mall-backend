package com.clickstechnology.Brillo.Mall.domain.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface AllRequestRepository extends JpaRepository<AllRequest, Long> {

    @Query("SELECT req FROM AllRequest req WHERE req.reference = :reference")
    Optional<AllRequest> findByReference (@Param("reference") String reference);
}
