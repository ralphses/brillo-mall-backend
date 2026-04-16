package com.clickstechnology.Brillo.Mall.domain.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByName(String name);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phoneNumber);
    Optional<Customer> findByUserId(String userId);

    @Query("SELECT c FROM Customer c WHERE c.email = :value OR c.phone = :value")
    Optional<Customer> findByEmailOrPhone(@Param("value") String value);

    Page<Customer> findAllByReferenceIn(Set<String> customerRefs, Pageable pageable);
    List<Customer> findAllByReferenceIn(Set<String> customerRefs);
}
