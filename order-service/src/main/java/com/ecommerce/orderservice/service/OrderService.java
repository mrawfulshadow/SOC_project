package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.PaymentWebhookRequest;
import com.ecommerce.orderservice.dto.UpdateDeliveryRequest;
import com.ecommerce.orderservice.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderByIdOrNumber(String identifier);

    Page<OrderResponse> getAllOrders(String customerId, OrderStatus status, Pageable pageable);

    OrderResponse updateOrderStatus(String id, UpdateOrderStatusRequest request);

    OrderResponse processPaymentWebhook(String id, PaymentWebhookRequest request);

    OrderResponse updateDeliveryDetails(String id, UpdateDeliveryRequest request);

    void cancelOrder(String id);
}
