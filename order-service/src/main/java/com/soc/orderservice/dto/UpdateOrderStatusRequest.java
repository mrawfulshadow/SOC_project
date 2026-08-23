package com.soc.orderservice.dto;

import com.soc.orderservice.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateOrderStatusRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;

    private String note;

    public UpdateOrderStatusRequest() {}

    public UpdateOrderStatusRequest(OrderStatus orderStatus, String note) {
        this.orderStatus = orderStatus;
        this.note = note;
    }

    public static UpdateOrderStatusRequestBuilder builder() {
        return new UpdateOrderStatusRequestBuilder();
    }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public static class UpdateOrderStatusRequestBuilder {
        private OrderStatus orderStatus;
        private String note;

        public UpdateOrderStatusRequestBuilder orderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; return this; }
        public UpdateOrderStatusRequestBuilder note(String note) { this.note = note; return this; }

        public UpdateOrderStatusRequest build() {
            return new UpdateOrderStatusRequest(orderStatus, note);
        }
    }
}
