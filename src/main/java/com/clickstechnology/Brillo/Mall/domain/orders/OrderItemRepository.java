package com.clickstechnology.Brillo.Mall.domain.orders;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
