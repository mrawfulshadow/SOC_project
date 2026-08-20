package com.soc.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMethod; // e.g., CREDIT_CARD, PAYPAL, BANK_TRANSFER, CASH_ON_DELIVERY

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED

    private String currency; // LKR, USD, EUR

    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
