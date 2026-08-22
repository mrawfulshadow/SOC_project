package com.soc.paymentservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionId;

    @Indexed
    private Long orderId;

    @Indexed
    private Long userId;

    private BigDecimal amount;

    private String paymentMethod; // e.g., CREDIT_CARD, PAYPAL, BANK_TRANSFER, CASH_ON_DELIVERY

    private String status; // PENDING, COMPLETED, FAILED, REFUNDED

    private String currency; // LKR, USD, EUR

    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

