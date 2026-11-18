package com.jobprocessing.repository;

import com.jobprocessing.model.SystemSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends MongoRepository<SystemSettings, String> {
    Optional<SystemSettings> findByKey(String key);
}
