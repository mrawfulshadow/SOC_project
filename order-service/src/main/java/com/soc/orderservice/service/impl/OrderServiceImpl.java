package com.soc.orderservice.service.impl;

import com.soc.orderservice.dto.CreateOrderRequest;
import com.soc.orderservice.dto.OrderItemRequest;
import com.soc.orderservice.dto.OrderResponse;
import com.soc.orderservice.dto.PaymentWebhookRequest;
import com.soc.orderservice.dto.UpdateDeliveryRequest;
import com.soc.orderservice.dto.UpdateOrderStatusRequest;
import com.soc.orderservice.exception.InvalidOrderStatusException;
import com.soc.orderservice.exception.InvalidWebhookSignatureException;
import com.soc.orderservice.exception.ResourceNotFoundException;
import com.soc.orderservice.exception.UnauthorizedAccessException;
import com.soc.orderservice.model.DeliveryInfo;
import com.soc.orderservice.model.DeliveryStatus;
import com.soc.orderservice.model.Order;
import com.soc.orderservice.model.OrderItem;
import com.soc.orderservice.model.OrderStatus;
import com.soc.orderservice.model.PaymentStatus;
import com.soc.orderservice.repository.OrderRepository;
import com.soc.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${webhook.secret:soc-payment-webhook-secret-key-2026}")
    private String webhookSecret;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customerId: {}", request.getCustomerId());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal subtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .unitPrice(itemReq.getUnitPrice())
                    .quantity(itemReq.getQuantity())
                    .subtotal(subtotal)
                    .build();
            orderItems.add(orderItem);
        }

        String generatedOrderNumber = generateOrderNumber();
        LocalDateTime now = LocalDateTime.now();

        DeliveryInfo initialDeliveryInfo = DeliveryInfo.builder()
                .deliveryStatus(DeliveryStatus.NOT_ASSIGNED)
                .estimatedDeliveryTime(now.plusDays(3))
                .dispatchNotes("Order placed successfully. Awaiting payment and processing.")
                .build();

        Order order = Order.builder()
                .orderNumber(generatedOrderNumber)
                .customerId(request.getCustomerId())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .shippingAddress(request.getShippingAddress())
                .items(orderItems)
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .deliveryInfo(initialDeliveryInfo)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with orderNumber: {} and ID: {}", savedOrder.getOrderNumber(), savedOrder.getId());
        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderByIdOrNumber(String identifier) {
        return getOrderByIdOrNumber(identifier, null, null);
    }

    @Override
    public OrderResponse getOrderByIdOrNumber(String identifier, String username, String role) {
        Order order = findOrderEntity(identifier);
        validateOwnershipOrAdmin(order, username, role);
        return mapToResponse(order);
    }

    @Override
    public Page<OrderResponse> getAllOrders(String customerId, OrderStatus status, Pageable pageable) {
        Page<Order> ordersPage;

        if (customerId != null && status != null) {
            ordersPage = orderRepository.findByCustomerIdAndOrderStatus(customerId, status, pageable);
        } else if (customerId != null) {
            ordersPage = orderRepository.findByCustomerId(customerId, pageable);
        } else if (status != null) {
            ordersPage = orderRepository.findByOrderStatus(status, pageable);
        } else {
            ordersPage = orderRepository.findAll(pageable);
        }

        return ordersPage.map(this::mapToResponse);
    }

    @Override
    public OrderResponse updateOrderStatus(String id, UpdateOrderStatusRequest request) {
        Order order = findOrderEntity(id);

        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException("Cannot change status of an order that is already " + order.getOrderStatus());
        }

        order.setOrderStatus(request.getOrderStatus());
        order.setUpdatedAt(LocalDateTime.now());

        if (request.getNote() != null && !request.getNote().isBlank()) {
            if (order.getDeliveryInfo() != null) {
                order.getDeliveryInfo().setDispatchNotes(request.getNote());
            }
        }

        Order updated = orderRepository.save(order);
        log.info("Order status updated to {} for orderId: {}", request.getOrderStatus(), id);
        return mapToResponse(updated);
    }

    @Override
    public OrderResponse processPaymentWebhook(String id, PaymentWebhookRequest request) {
        return processPaymentWebhook(id, null, request);
    }

    @Override
    public OrderResponse processPaymentWebhook(String id, String signature, PaymentWebhookRequest request) {
        if (!isValidWebhookSignature(signature, request)) {
            throw new InvalidWebhookSignatureException("Unauthorized: Missing or invalid HMAC-SHA256 signature in X-Signature-SHA256 header");
        }

        Order order = findOrderEntity(id);

        log.info("Processing payment webhook for orderId: {}, transactionId: {}, status: {}",
                id, request.getTransactionId(), request.getPaymentStatus());

        order.setPaymentStatus(request.getPaymentStatus());
        order.setTransactionId(request.getTransactionId());
        order.setUpdatedAt(LocalDateTime.now());

        if (request.getPaymentStatus() == PaymentStatus.PAID) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            if (order.getDeliveryInfo() != null) {
                order.getDeliveryInfo().setDispatchNotes("Payment confirmed. Ready for processing & dispatch.");
            }
        } else if (request.getPaymentStatus() == PaymentStatus.FAILED) {
            if (order.getDeliveryInfo() != null) {
                order.getDeliveryInfo().setDispatchNotes("Payment failed: " + request.getGatewayResponseMessage());
            }
        }

        Order updated = orderRepository.save(order);
        return mapToResponse(updated);
    }

    private boolean isValidWebhookSignature(String signature, PaymentWebhookRequest request) {
        if (signature == null || signature.isBlank()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            // 1. Canonical data string (transactionId:paymentStatus)
            String canonicalData = request.getTransactionId() + ":" + request.getPaymentStatus();
            byte[] hashCanonical = mac.doFinal(canonicalData.getBytes(StandardCharsets.UTF_8));
            String hexCanonical = toHex(hashCanonical);

            // 2. JSON representation
            mac.reset();
            String jsonData = objectMapper.writeValueAsString(request);
            byte[] hashJson = mac.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));
            String hexJson = toHex(hashJson);

            byte[] sigBytes = signature.toLowerCase().getBytes(StandardCharsets.UTF_8);

            return MessageDigest.isEqual(hexCanonical.getBytes(StandardCharsets.UTF_8), sigBytes)
                    || MessageDigest.isEqual(hexJson.getBytes(StandardCharsets.UTF_8), sigBytes)
                    || MessageDigest.isEqual(Base64.getEncoder().encodeToString(hashCanonical).getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))
                    || MessageDigest.isEqual(Base64.getEncoder().encodeToString(hashJson).getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to verify HMAC webhook signature", e);
            return false;
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public OrderResponse updateDeliveryDetails(String id, UpdateDeliveryRequest request) {
        Order order = findOrderEntity(id);

        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        if (deliveryInfo == null) {
            deliveryInfo = new DeliveryInfo();
        }

        if (request.getDeliveryStatus() != null) {
            deliveryInfo.setDeliveryStatus(request.getDeliveryStatus());

            if (request.getDeliveryStatus() == DeliveryStatus.PICKED_UP || request.getDeliveryStatus() == DeliveryStatus.IN_TRANSIT) {
                order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
            } else if (request.getDeliveryStatus() == DeliveryStatus.DELIVERED) {
                order.setOrderStatus(OrderStatus.DELIVERED);
            }
        }

        if (request.getCarrier() != null) {
            deliveryInfo.setCarrier(request.getCarrier());
        }
        if (request.getTrackingNumber() != null) {
            deliveryInfo.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getEstimatedDeliveryTime() != null) {
            deliveryInfo.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());
        }
        if (request.getDispatchNotes() != null) {
            deliveryInfo.setDispatchNotes(request.getDispatchNotes());
        }

        order.setDeliveryInfo(deliveryInfo);
        order.setUpdatedAt(LocalDateTime.now());

        Order updated = orderRepository.save(order);
        log.info("Delivery info updated for orderId: {}, new delivery status: {}", id, deliveryInfo.getDeliveryStatus());
        return mapToResponse(updated);
    }

    @Override
    public void cancelOrder(String id) {
        cancelOrder(id, null, null);
    }

    @Override
    public void cancelOrder(String id, String username, String role) {
        Order order = findOrderEntity(id);
        validateOwnershipOrAdmin(order, username, role);

        if (order.getOrderStatus() == OrderStatus.DELIVERED || order.getOrderStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            throw new InvalidOrderStatusException("Cannot cancel an order that is out for delivery or delivered.");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        if (order.getDeliveryInfo() != null) {
            order.getDeliveryInfo().setDispatchNotes("Order cancelled by customer or admin.");
        }

        orderRepository.save(order);
        log.info("Order cancelled successfully: {}", id);
    }

    private void validateOwnershipOrAdmin(Order order, String username, String role) {
        if (username == null && role == null) {
            return;
        }
        boolean isAdmin = "ROLE_ADMIN".equals(role);
        boolean isOwner = username != null && username.equals(order.getCustomerId());
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedAccessException("Access Denied: You do not have permission to access or modify this order");
        }
    }

    private Order findOrderEntity(String identifier) {
        return orderRepository.findById(identifier)
                .orElseGet(() -> orderRepository.findByOrderNumber(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID or Order Number: " + identifier)));
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + timestamp + "-" + randomSuffix;
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .shippingAddress(order.getShippingAddress())
                .items(order.getItems())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .transactionId(order.getTransactionId())
                .deliveryInfo(order.getDeliveryInfo())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
