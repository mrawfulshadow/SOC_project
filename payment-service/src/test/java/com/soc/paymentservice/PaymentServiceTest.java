package com.soc.paymentservice;

import com.soc.paymentservice.controller.PaymentController;
import com.soc.paymentservice.dto.PaymentRequestDTO;
import com.soc.paymentservice.dto.PaymentResponseDTO;
import com.soc.paymentservice.model.Payment;
import com.soc.paymentservice.repository.PaymentRepository;
import com.soc.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentRequestDTO validRequest;
    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        validRequest = PaymentRequestDTO.builder()
                .orderId(101L)
                .userId(1L)
                .amount(new BigDecimal("1500.00"))
                .paymentMethod("CREDIT_CARD")
                .currency("LKR")
                .notes("Order payment")
                .build();

        samplePayment = Payment.builder()
                .id("pay1")
                .transactionId("TXN-12345678")
                .orderId(101L)
                .userId(1L)
                .amount(new BigDecimal("1500.00"))
                .paymentMethod("CREDIT_CARD")
                .status("COMPLETED")
                .currency("LKR")
                .notes("Order payment")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testProcessPayment_Success_Completed() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId("pay1");
            return p;
        });

        PaymentResponseDTO response = paymentService.processPayment(validRequest);

        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("TXN-"));
        assertEquals("LKR", response.getCurrency());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testProcessPayment_InvalidAmount_Failed() {
        PaymentRequestDTO invalidReq = PaymentRequestDTO.builder()
                .orderId(102L)
                .userId(1L)
                .amount(BigDecimal.ZERO)
                .paymentMethod("CREDIT_CARD")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDTO response = paymentService.processPayment(invalidReq);

        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertEquals("Invalid transaction amount", response.getNotes());
    }

    @Test
    void testRefundPayment_Success() {
        when(paymentRepository.findById("pay1")).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<PaymentResponseDTO> refunded = paymentService.refundPayment("pay1", "Customer requested cancellation");

        assertTrue(refunded.isPresent());
        assertEquals("REFUNDED", refunded.get().getStatus());
        assertEquals("Customer requested cancellation", refunded.get().getNotes());
    }

    @Test
    void testRefundPayment_NonExistent_ReturnsEmpty() {
        when(paymentRepository.findById("invalid")).thenReturn(Optional.empty());

        Optional<PaymentResponseDTO> refunded = paymentService.refundPayment("invalid", "Reason");
        assertTrue(refunded.isEmpty());
    }

    @Test
    void testController_Refund_AdminAllowed() {
        ReflectionTestUtils.setField(paymentController, "paymentService", paymentService);
        when(paymentRepository.findById("pay1")).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = paymentController.refundPayment("ROLE_ADMIN", "pay1", Map.of("reason", "Defective product"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testController_Refund_UserForbidden() {
        ReflectionTestUtils.setField(paymentController, "paymentService", paymentService);

        ResponseEntity<?> response = paymentController.refundPayment("ROLE_USER", "pay1", Map.of("reason", "Refund"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testController_GetHistory_OwnerAllowed() {
        ReflectionTestUtils.setField(paymentController, "paymentService", paymentService);
        when(paymentRepository.findByUserId(1L)).thenReturn(List.of(samplePayment));

        ResponseEntity<?> response = paymentController.getPaymentHistory("1", "ROLE_USER", 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testController_GetHistory_AttackerForbidden() {
        ReflectionTestUtils.setField(paymentController, "paymentService", paymentService);

        ResponseEntity<?> response = paymentController.getPaymentHistory("2", "ROLE_USER", 1L);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
