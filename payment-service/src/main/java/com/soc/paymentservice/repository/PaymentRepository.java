package com.soc.paymentservice.repository;

import com.soc.paymentservice.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByUserId(Long userId);
    List<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);
}

