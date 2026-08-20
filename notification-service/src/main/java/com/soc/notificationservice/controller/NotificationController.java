package com.soc.notificationservice.controller;

import com.soc.notificationservice.model.Notification;
import com.soc.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository repository;

    @PostMapping("/email")
    public ResponseEntity<Notification> sendEmailNotification(@RequestBody Notification notification) {
        notification.setType("EMAIL");
        notification.setStatus("SENT");
        notification.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(repository.save(notification));
    }

    @PostMapping("/sms")
    public ResponseEntity<Notification> sendSmsNotification(@RequestBody Notification notification) {
        notification.setType("SMS");
        notification.setStatus("SENT");
        notification.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(repository.save(notification));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(repository.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
