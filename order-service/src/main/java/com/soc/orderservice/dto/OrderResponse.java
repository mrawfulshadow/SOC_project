package com.soc.orderservice.dto;

import com.soc.orderservice.model.Address;
import com.soc.orderservice.model.DeliveryInfo;
import com.soc.orderservice.model.OrderItem;
import com.soc.orderservice.model.OrderStatus;
import com.soc.orderservice.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private String id;
    private String orderNumber;
    private String customerId;
    private String customerEmail;
    private String customerPhone;
    private Address shippingAddress;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private DeliveryInfo deliveryInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponse() {}

    public OrderResponse(String id, String orderNumber, String customerId, String customerEmail, String customerPhone, Address shippingAddress, List<OrderItem> items, BigDecimal totalAmount, OrderStatus orderStatus, PaymentStatus paymentStatus, String transactionId, DeliveryInfo deliveryInfo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
        this.deliveryInfo = deliveryInfo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderResponseBuilder builder() {
        return new OrderResponseBuilder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public DeliveryInfo getDeliveryInfo() { return deliveryInfo; }
    public void setDeliveryInfo(DeliveryInfo deliveryInfo) { this.deliveryInfo = deliveryInfo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class OrderResponseBuilder {
        private String id;
        private String orderNumber;
        private String customerId;
        private String customerEmail;
        private String customerPhone;
        private Address shippingAddress;
        private List<OrderItem> items;
        private BigDecimal totalAmount;
        private OrderStatus orderStatus;
        private PaymentStatus paymentStatus;
        private String transactionId;
        private DeliveryInfo deliveryInfo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public OrderResponseBuilder id(String id) { this.id = id; return this; }
        public OrderResponseBuilder orderNumber(String orderNumber) { this.orderNumber = orderNumber; return this; }
        public OrderResponseBuilder customerId(String customerId) { this.customerId = customerId; return this; }
        public OrderResponseBuilder customerEmail(String customerEmail) { this.customerEmail = customerEmail; return this; }
        public OrderResponseBuilder customerPhone(String customerPhone) { this.customerPhone = customerPhone; return this; }
        public OrderResponseBuilder shippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; return this; }
        public OrderResponseBuilder items(List<OrderItem> items) { this.items = items; return this; }
        public OrderResponseBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public OrderResponseBuilder orderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; return this; }
        public OrderResponseBuilder paymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public OrderResponseBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public OrderResponseBuilder deliveryInfo(DeliveryInfo deliveryInfo) { this.deliveryInfo = deliveryInfo; return this; }
        public OrderResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public OrderResponse build() {
            return new OrderResponse(id, orderNumber, customerId, customerEmail, customerPhone, shippingAddress, items, totalAmount, orderStatus, paymentStatus, transactionId, deliveryInfo, createdAt, updatedAt);
        }
    }
}
