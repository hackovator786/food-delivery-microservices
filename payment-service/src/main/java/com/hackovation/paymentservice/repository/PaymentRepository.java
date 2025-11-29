package com.hackovation.paymentservice.repository;

import com.hackovation.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndRazorpayOrderId(String orderId, String razorpayOrderId);
}
