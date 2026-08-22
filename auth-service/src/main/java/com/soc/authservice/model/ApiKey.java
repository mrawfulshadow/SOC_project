package com.soc.authservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "api_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    private String id;

    @Indexed(unique = true)
    private String service;

    @Indexed(unique = true)
    private String apiKey;

    private String headerName;
    private int targetPort;
    private String status;
    private String description;
    private LocalDateTime createdAt;
}
