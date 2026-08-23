package com.soc.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    private Long orderId;

    @NotBlank(message = "Recipient address or phone number is required")
    private String recipient;

    @NotBlank(message = "Message content is required")
    private String message;
}
