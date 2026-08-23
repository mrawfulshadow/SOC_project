package com.soc.orderservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @Indexed(unique = true)
    private String orderNumber;

    @Indexed
    private String customerId;

    private String customerEmail;
    private String customerPhone;

    private Address shippingAddress;

    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

    private OrderStatus orderStatus = OrderStatus.PENDING;

    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String transactionId;

    private DeliveryInfo deliveryInfo = new DeliveryInfo();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {}

    public Order(String id, String orderNumber, String customerId, String customerEmail, String customerPhone, Address shippingAddress, List<OrderItem> items, BigDecimal totalAmount, OrderStatus orderStatus, PaymentStatus paymentStatus, String transactionId, DeliveryInfo deliveryInfo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.items = items != null ? items : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus != null ? orderStatus : OrderStatus.PENDING;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.PENDING;
        this.transactionId = transactionId;
        this.deliveryInfo = deliveryInfo != null ? deliveryInfo : new DeliveryInfo();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
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

    public static class OrderBuilder {
        private String id;
        private String orderNumber;
        private String customerId;
        private String customerEmail;
        private String customerPhone;
        private Address shippingAddress;
        private List<OrderItem> items = new ArrayList<>();
        private BigDecimal totalAmount;
        private OrderStatus orderStatus = OrderStatus.PENDING;
        private PaymentStatus paymentStatus = PaymentStatus.PENDING;
        private String transactionId;
        private DeliveryInfo deliveryInfo = new DeliveryInfo();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public OrderBuilder id(String id) { this.id = id; return this; }
        public OrderBuilder orderNumber(String orderNumber) { this.orderNumber = orderNumber; return this; }
        public OrderBuilder customerId(String customerId) { this.customerId = customerId; return this; }
        public OrderBuilder customerEmail(String customerEmail) { this.customerEmail = customerEmail; return this; }
        public OrderBuilder customerPhone(String customerPhone) { this.customerPhone = customerPhone; return this; }
        public OrderBuilder shippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; return this; }
        public OrderBuilder items(List<OrderItem> items) { this.items = items; return this; }
        public OrderBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public OrderBuilder orderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; return this; }
        public OrderBuilder paymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public OrderBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public OrderBuilder deliveryInfo(DeliveryInfo deliveryInfo) { this.deliveryInfo = deliveryInfo; return this; }
        public OrderBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Order build() {
            return new Order(id, orderNumber, customerId, customerEmail, customerPhone, shippingAddress, items, totalAmount, orderStatus, paymentStatus, transactionId, deliveryInfo, createdAt, updatedAt);
        }
    }
}
