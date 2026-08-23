package com.soc.notificationservice.controller;

import com.soc.notificationservice.dto.NotificationRequestDTO;
import com.soc.notificationservice.dto.NotificationResponseDTO;
import com.soc.notificationservice.model.Notification;
import com.soc.notificationservice.repository.NotificationRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository repository;

    @PostMapping("/email")
    public ResponseEntity<NotificationResponseDTO> sendEmailNotification(@Valid @RequestBody NotificationRequestDTO request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .recipient(request.getRecipient())
                .message(request.getMessage())
                .type("EMAIL")
                .status("SENT")
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = repository.save(notification);
        return new ResponseEntity<>(mapToResponse(saved), HttpStatus.CREATED);
    }

    @PostMapping("/sms")
    public ResponseEntity<NotificationResponseDTO> sendSmsNotification(@Valid @RequestBody NotificationRequestDTO request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .recipient(request.getRecipient())
                .message(request.getMessage())
                .type("SMS")
                .status("SENT")
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = repository.save(notification);
        return new ResponseEntity<>(mapToResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByUser(@PathVariable Long userId) {
        List<NotificationResponseDTO> list = repository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(@PathVariable String id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private NotificationResponseDTO mapToResponse(Notification notification) {
        if (notification == null) return null;
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .orderId(notification.getOrderId())
                .recipient(notification.getRecipient())
                .type(notification.getType())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
