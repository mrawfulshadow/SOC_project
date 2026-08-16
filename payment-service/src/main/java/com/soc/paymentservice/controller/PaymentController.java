package com.soc.paymentservice.controller;

import com.soc.paymentservice.model.Payment;
import com.soc.paymentservice.service.PaymentService;
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
    public ResponseEntity<Payment> processPayment(@RequestBody Payment payment) {
        Payment processed = paymentService.processPayment(payment);
        return new ResponseEntity<>(processed, HttpStatus.CREATED);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Payment>> getPaymentHistory(@PathVariable Long userId) {
        List<Payment> history = paymentService.getPaymentHistoryByUserId(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        return paymentService.getPaymentByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/refund/{id}")
    public ResponseEntity<?> refundPayment(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String reason = (request != null) ? request.get("reason") : "Customer request";
        return paymentService.refundPayment(id, reason)
                .map(payment -> ResponseEntity.ok((Object) payment))
                .orElse(ResponseEntity.badRequest().body("Refund failed: Payment not found or cannot be refunded"));
    }
}
