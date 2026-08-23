package com.soc.paymentservice.controller;

import com.soc.paymentservice.dto.PaymentRequestDTO;
import com.soc.paymentservice.dto.PaymentResponseDTO;
import com.soc.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO payment) {
        PaymentResponseDTO processed = paymentService.processPayment(payment);
        return new ResponseEntity<>(processed, HttpStatus.CREATED);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getPaymentHistory(
            @RequestHeader(value = "X-User-Name", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long userId) {
        boolean isAdmin = "ROLE_ADMIN".equals(role);
        boolean isOwner = username != null && (username.equals(String.valueOf(userId)) || username.equals(userId.toString()));

        if (role != null && !isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You do not have permission to view payment history for this user");
        }

        List<PaymentResponseDTO> history = paymentService.getPaymentHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable String id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByTransactionId(@PathVariable String transactionId) {
        return paymentService.getPaymentByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/refund/{id}")
    public ResponseEntity<?> refundPayment(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> request) {
        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only users with ROLE_ADMIN can process refunds");
        }
        String reason = (request != null) ? request.get("reason") : "Customer request";
        return paymentService.refundPayment(id, reason)
                .map(payment -> ResponseEntity.ok((Object) payment))
                .orElse(ResponseEntity.badRequest().body("Refund failed: Payment not found or cannot be refunded"));
    }
}
