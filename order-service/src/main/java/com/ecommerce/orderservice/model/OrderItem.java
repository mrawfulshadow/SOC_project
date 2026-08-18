package com.ecommerce.orderservice.model;

import java.math.BigDecimal;

public class OrderItem {
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public OrderItem() {}

    public OrderItem(String productId, String productName, BigDecimal unitPrice, Integer quantity, BigDecimal subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public static OrderItemBuilder builder() {
        return new OrderItemBuilder();
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public static class OrderItemBuilder {
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;

        public OrderItemBuilder productId(String productId) { this.productId = productId; return this; }
        public OrderItemBuilder productName(String productName) { this.productName = productName; return this; }
        public OrderItemBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public OrderItemBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderItemBuilder subtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }

        public OrderItem build() {
            return new OrderItem(productId, productName, unitPrice, quantity, subtotal);
        }
    }
}
