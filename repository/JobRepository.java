package com.example.api.repository;

import com.example.api.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobRepository extends MongoRepository<Job,String> {

}
