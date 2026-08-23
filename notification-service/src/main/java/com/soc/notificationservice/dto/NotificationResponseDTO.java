package com.soc.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private String id;
    private Long userId;
    private Long orderId;
    private String recipient;
    private String type;
    private String message;
    private String status;
    private LocalDateTime createdAt;
}
