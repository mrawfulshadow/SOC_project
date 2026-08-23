package com.soc.paymentservice.service;

import com.soc.paymentservice.dto.PaymentRequestDTO;
import com.soc.paymentservice.dto.PaymentResponseDTO;
import com.soc.paymentservice.model.Payment;
import com.soc.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String currency = (request.getCurrency() != null && !request.getCurrency().isEmpty()) ? request.getCurrency() : "LKR";

        String status;
        String notes = request.getNotes();

        // Server-side validation and processing
        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            status = "COMPLETED";
        } else {
            status = "FAILED";
            notes = "Invalid transaction amount";
        }

        LocalDateTime now = LocalDateTime.now();

        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(status)
                .currency(currency)
                .notes(notes)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    public List<PaymentResponseDTO> getPaymentHistoryByUserId(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<PaymentResponseDTO> getPaymentById(String id) {
        return paymentRepository.findById(id).map(this::mapToResponse);
    }

    public Optional<PaymentResponseDTO> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId).map(this::mapToResponse);
    }

    public Optional<PaymentResponseDTO> refundPayment(String id, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            if ("COMPLETED".equalsIgnoreCase(payment.getStatus())) {
                payment.setStatus("REFUNDED");
                payment.setNotes(reason != null ? reason : "Refund processed successfully");
                payment.setUpdatedAt(LocalDateTime.now());
                Payment updated = paymentRepository.save(payment);
                return Optional.of(mapToResponse(updated));
            }
        }
        return Optional.empty();
    }

    public PaymentResponseDTO mapToResponse(Payment payment) {
        if (payment == null) return null;
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .currency(payment.getCurrency())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}

