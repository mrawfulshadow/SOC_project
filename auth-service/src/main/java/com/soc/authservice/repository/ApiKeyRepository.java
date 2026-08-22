package com.soc.authservice.repository;

import com.soc.authservice.model.ApiKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    Optional<ApiKey> findByService(String service);
    Optional<ApiKey> findByApiKey(String apiKey);
}
