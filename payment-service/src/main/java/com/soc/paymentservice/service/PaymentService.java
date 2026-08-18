package com.soc.paymentservice.service;

import com.soc.paymentservice.model.Payment;
import com.soc.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment processPayment(Payment payment) {
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        if (payment.getCurrency() == null || payment.getCurrency().isEmpty()) {
            payment.setCurrency("LKR");
        }
        
        // Mock processing logic: Amount <= 0 fails, otherwise completes
        if (payment.getAmount() != null && payment.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            payment.setStatus("COMPLETED");
        } else {
            payment.setStatus("FAILED");
            payment.setNotes("Invalid transaction amount");
        }
        
        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentHistoryByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    public Optional<Payment> refundPayment(Long id, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            if ("COMPLETED".equalsIgnoreCase(payment.getStatus())) {
                payment.setStatus("REFUNDED");
                payment.setNotes(reason != null ? reason : "Refund processed successfully");
                payment.setUpdatedAt(LocalDateTime.now());
                return Optional.of(paymentRepository.save(payment));
            }
        }
        return Optional.empty();
    }
}
