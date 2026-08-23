package com.soc.notificationservice;

import com.soc.notificationservice.controller.NotificationController;
import com.soc.notificationservice.dto.NotificationRequestDTO;
import com.soc.notificationservice.dto.NotificationResponseDTO;
import com.soc.notificationservice.model.Notification;
import com.soc.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationController controller;

    private NotificationRequestDTO emailReq;
    private NotificationRequestDTO smsReq;
    private Notification emailNotification;

    @BeforeEach
    void setUp() {
        emailReq = NotificationRequestDTO.builder()
                .userId(1L)
                .orderId(100L)
                .recipient("test@example.com")
                .message("Your order has been placed")
                .build();

        smsReq = NotificationRequestDTO.builder()
                .userId(1L)
                .orderId(100L)
                .recipient("+94771234567")
                .message("Your order has been shipped")
                .build();

        emailNotification = Notification.builder()
                .id("notif1")
                .userId(1L)
                .orderId(100L)
                .recipient("test@example.com")
                .type("EMAIL")
                .message("Your order has been placed")
                .status("SENT")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testSendEmailNotification() {
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId("notif1");
            return n;
        });

        ResponseEntity<NotificationResponseDTO> response = controller.sendEmailNotification(emailReq);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("EMAIL", response.getBody().getType());
        assertEquals("SENT", response.getBody().getStatus());
        assertEquals("test@example.com", response.getBody().getRecipient());
        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendSmsNotification() {
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId("notif2");
            return n;
        });

        ResponseEntity<NotificationResponseDTO> response = controller.sendSmsNotification(smsReq);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SMS", response.getBody().getType());
        assertEquals("SENT", response.getBody().getStatus());
        assertEquals("+94771234567", response.getBody().getRecipient());
        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void testGetNotificationsByUser() {
        when(repository.findByUserId(1L)).thenReturn(List.of(emailNotification));

        ResponseEntity<List<NotificationResponseDTO>> response = controller.getNotificationsByUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("notif1", response.getBody().get(0).getId());
    }

    @Test
    void testGetNotificationById_Found() {
        when(repository.findById("notif1")).thenReturn(Optional.of(emailNotification));

        ResponseEntity<NotificationResponseDTO> response = controller.getNotificationById("notif1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("notif1", response.getBody().getId());
    }

    @Test
    void testGetNotificationById_NotFound() {
        when(repository.findById("invalid")).thenReturn(Optional.empty());

        ResponseEntity<NotificationResponseDTO> response = controller.getNotificationById("invalid");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
