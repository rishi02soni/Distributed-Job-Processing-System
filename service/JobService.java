package com.example.api.service;

import com.example.api.model.Job;
import com.example.api.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    public Job createJob(Job job){

        job.setStatus("PENDING");

        Job saved = jobRepository.save(job);

        redisTemplate.opsForList()
                .leftPush("job_queue", saved.getId());

        return saved;
    }
}
