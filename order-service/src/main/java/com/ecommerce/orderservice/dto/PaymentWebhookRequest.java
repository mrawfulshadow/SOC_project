package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PaymentWebhookRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    private BigDecimal amountPaid;
    private String paymentMethod;
    private String gatewayResponseMessage;

    public PaymentWebhookRequest() {}

    public PaymentWebhookRequest(String transactionId, PaymentStatus paymentStatus, BigDecimal amountPaid, String paymentMethod, String gatewayResponseMessage) {
        this.transactionId = transactionId;
        this.paymentStatus = paymentStatus;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.gatewayResponseMessage = gatewayResponseMessage;
    }

    public static PaymentWebhookRequestBuilder builder() {
        return new PaymentWebhookRequestBuilder();
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getGatewayResponseMessage() { return gatewayResponseMessage; }
    public void setGatewayResponseMessage(String gatewayResponseMessage) { this.gatewayResponseMessage = gatewayResponseMessage; }

    public static class PaymentWebhookRequestBuilder {
        private String transactionId;
        private PaymentStatus paymentStatus;
        private BigDecimal amountPaid;
        private String paymentMethod;
        private String gatewayResponseMessage;

        public PaymentWebhookRequestBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public PaymentWebhookRequestBuilder paymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public PaymentWebhookRequestBuilder amountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; return this; }
        public PaymentWebhookRequestBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public PaymentWebhookRequestBuilder gatewayResponseMessage(String gatewayResponseMessage) { this.gatewayResponseMessage = gatewayResponseMessage; return this; }

        public PaymentWebhookRequest build() {
            return new PaymentWebhookRequest(transactionId, paymentStatus, amountPaid, paymentMethod, gatewayResponseMessage);
        }
    }
}
