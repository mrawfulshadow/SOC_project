package com.soc.orderservice.dto;

import com.soc.orderservice.model.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateOrderRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    private String customerPhone;

    @NotNull(message = "Shipping address is required")
    @Valid
    private Address shippingAddress;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {}

    public CreateOrderRequest(String customerId, String customerEmail, String customerPhone, Address shippingAddress, List<OrderItemRequest> items) {
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.items = items;
    }

    public static CreateOrderRequestBuilder builder() {
        return new CreateOrderRequestBuilder();
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public static class CreateOrderRequestBuilder {
        private String customerId;
        private String customerEmail;
        private String customerPhone;
        private Address shippingAddress;
        private List<OrderItemRequest> items;

        public CreateOrderRequestBuilder customerId(String customerId) { this.customerId = customerId; return this; }
        public CreateOrderRequestBuilder customerEmail(String customerEmail) { this.customerEmail = customerEmail; return this; }
        public CreateOrderRequestBuilder customerPhone(String customerPhone) { this.customerPhone = customerPhone; return this; }
        public CreateOrderRequestBuilder shippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; return this; }
        public CreateOrderRequestBuilder items(List<OrderItemRequest> items) { this.items = items; return this; }

        public CreateOrderRequest build() {
            return new CreateOrderRequest(customerId, customerEmail, customerPhone, shippingAddress, items);
        }
    }
}
