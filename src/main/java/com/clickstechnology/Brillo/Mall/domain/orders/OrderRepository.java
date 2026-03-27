package com.clickstechnology.Brillo.Mall.domain.orders;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByReference(String reference);

    Optional<Order> findByOrderId(String orderId);
}