package com.soc.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrderItemRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public OrderItemRequest() {}

    public OrderItemRequest(String productId, String productName, BigDecimal unitPrice, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public static OrderItemRequestBuilder builder() {
        return new OrderItemRequestBuilder();
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public static class OrderItemRequestBuilder {
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;

        public OrderItemRequestBuilder productId(String productId) { this.productId = productId; return this; }
        public OrderItemRequestBuilder productName(String productName) { this.productName = productName; return this; }
        public OrderItemRequestBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public OrderItemRequestBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }

        public OrderItemRequest build() {
            return new OrderItemRequest(productId, productName, unitPrice, quantity);
        }
    }
}
