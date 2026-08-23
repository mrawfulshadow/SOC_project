package com.soc.orderservice.repository;

import com.soc.orderservice.model.Order;
import com.soc.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerId(String customerId);

    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    Page<Order> findByCustomerIdAndOrderStatus(String customerId, OrderStatus orderStatus, Pageable pageable);
}
