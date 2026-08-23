package com.soc.orderservice.service;

import com.soc.orderservice.dto.CreateOrderRequest;
import com.soc.orderservice.dto.OrderResponse;
import com.soc.orderservice.dto.PaymentWebhookRequest;
import com.soc.orderservice.dto.UpdateDeliveryRequest;
import com.soc.orderservice.dto.UpdateOrderStatusRequest;
import com.soc.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderByIdOrNumber(String identifier);

    OrderResponse getOrderByIdOrNumber(String identifier, String username, String role);

    Page<OrderResponse> getAllOrders(String customerId, OrderStatus status, Pageable pageable);

    OrderResponse updateOrderStatus(String id, UpdateOrderStatusRequest request);

    OrderResponse processPaymentWebhook(String id, PaymentWebhookRequest request);

    OrderResponse processPaymentWebhook(String id, String signature, PaymentWebhookRequest request);

    OrderResponse updateDeliveryDetails(String id, UpdateDeliveryRequest request);

    void cancelOrder(String id);

    void cancelOrder(String id, String username, String role);
}
