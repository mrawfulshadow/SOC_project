package com.soc.notificationservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;

    private Long userId;
    private Long orderId;
    private String recipient;
    private String type;      // EMAIL or SMS
    private String message;
    private String status;   // SENT, FAILED
    private LocalDateTime createdAt;
}
