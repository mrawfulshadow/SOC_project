package com.soc.orderservice.service;

import com.soc.orderservice.dto.CreateOrderRequest;
import com.soc.orderservice.dto.OrderItemRequest;
import com.soc.orderservice.dto.OrderResponse;
import com.soc.orderservice.model.Address;
import com.soc.orderservice.model.Order;
import com.soc.orderservice.model.OrderStatus;
import com.soc.orderservice.repository.OrderRepository;
import com.soc.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        Address address = Address.builder()
                .street("123 Main St")
                .city("Colombo")
                .state("Western")
                .zipCode("00100")
                .country("Sri Lanka")
                .build();

        OrderItemRequest item1 = OrderItemRequest.builder()
                .productId("PROD-001")
                .productName("Wireless Headphones")
                .unitPrice(new BigDecimal("150.00"))
                .quantity(2)
                .build();

        OrderItemRequest item2 = OrderItemRequest.builder()
                .productId("PROD-002")
                .productName("USB-C Cable")
                .unitPrice(new BigDecimal("15.00"))
                .quantity(1)
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .customerId("CUST-999")
                .customerEmail("customer@example.com")
                .customerPhone("+94771234567")
                .shippingAddress(address)
                .items(List.of(item1, item2))
                .build();
    }

    @Test
    void createOrder_shouldCalculateTotalAndSaveOrder() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId("60d5ec49f1b2c8234c895234");
            return savedOrder;
        });

        OrderResponse response = orderService.createOrder(createOrderRequest);

        assertNotNull(response);
        assertEquals("60d5ec49f1b2c8234c895234", response.getId());
        assertEquals("CUST-999", response.getCustomerId());
        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        assertEquals(2, response.getItems().size());
        assertEquals(new BigDecimal("315.00"), response.getTotalAmount());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getOrderByIdOrNumber_shouldReturnOrderWhenFound() {
        Order mockOrder = Order.builder()
                .id("60d5ec49f1b2c8234c895234")
                .orderNumber("ORD-20260811-123456")
                .customerId("CUST-999")
                .totalAmount(new BigDecimal("315.00"))
                .build();

        when(orderRepository.findById("60d5ec49f1b2c8234c895234")).thenReturn(Optional.of(mockOrder));

        OrderResponse response = orderService.getOrderByIdOrNumber("60d5ec49f1b2c8234c895234");

        assertNotNull(response);
        assertEquals("ORD-20260811-123456", response.getOrderNumber());
    }
}
