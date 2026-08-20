package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.ApiResponse;
import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.PaymentWebhookRequest;
import com.ecommerce.orderservice.dto.UpdateDeliveryRequest;
import com.ecommerce.orderservice.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Controller", description = "APIs for managing customer orders, status tracking, payment webhooks & delivery updates")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place a new Order", description = "Creates a new order, calculates totals, and sets status to PENDING")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse createdOrder = orderService.createOrder(request);
        return new ResponseEntity<>(ApiResponse.success("Order created successfully", createdOrder), HttpStatus.CREATED);
    }

    @GetMapping("/{identifier}")
    @Operation(summary = "Get Order Details", description = "Fetches order details by Mongo ID or Order Number (e.g. ORD-20260811-XXXXXX)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByIdOrNumber(
            @Parameter(description = "Order Mongo ID or Business Order Number") @PathVariable String identifier) {
        OrderResponse order = orderService.getOrderByIdOrNumber(identifier);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    @GetMapping
    @Operation(summary = "List Orders", description = "Retrieves paginated list of orders with optional filters by customerId and orderStatus")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> orders = orderService.getAllOrders(customerId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Order Status", description = "Updates order status (e.g. CONFIRMED, PROCESSING, CANCELLED)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse updated = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updated));
    }

    @PostMapping("/{id}/payment-webhook")
    @Operation(summary = "Process Payment Webhook Callback", description = "Callback endpoint for payment gateway to notify payment result (PAID/FAILED)")
    public ResponseEntity<ApiResponse<OrderResponse>> processPaymentWebhook(
            @PathVariable String id,
            @Valid @RequestBody PaymentWebhookRequest request) {
        OrderResponse updated = orderService.processPaymentWebhook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment status updated via webhook", updated));
    }

    @PatchMapping("/{id}/delivery")
    @Operation(summary = "Update Delivery Dispatch Info", description = "Updates tracking number, carrier, and delivery status (IN_TRANSIT, DELIVERED)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateDeliveryDetails(
            @PathVariable String id,
            @Valid @RequestBody UpdateDeliveryRequest request) {
        OrderResponse updated = orderService.updateDeliveryDetails(id, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery details updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel Order", description = "Cancels an active order if it has not been delivered or dispatched")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable String id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", null));
    }
}
