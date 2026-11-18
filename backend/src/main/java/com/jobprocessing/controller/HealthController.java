package com.jobprocessing.controller;

import com.jobprocessing.dto.ApiResponse;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final MongoTemplate mongoTemplate;
    private final RedisConnectionFactory redisConnectionFactory;

    public HealthController(MongoTemplate mongoTemplate, RedisConnectionFactory redisConnectionFactory) {
        this.mongoTemplate = mongoTemplate;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        boolean mongoHealthy = false;
        boolean redisHealthy = false;

        try {
            mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            mongoHealthy = true;
        } catch (Exception e) {
            details.put("mongoError", e.getMessage());
        }

        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            String ping = conn.ping();
            redisHealthy = "PONG".equalsIgnoreCase(ping);
        } catch (Exception e) {
            details.put("redisError", e.getMessage());
        }

        details.put("status", (mongoHealthy && redisHealthy) ? "UP" : "DEGRADED");
        details.put("mongoDb", mongoHealthy ? "CONNECTED" : "DISCONNECTED");
        details.put("redis", redisHealthy ? "CONNECTED" : "DISCONNECTED");
        details.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(ApiResponse.ok("System Health Status", details));
    }
}
