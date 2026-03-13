package com.example.api.controller;

import com.example.api.model.Job;
import com.example.api.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public Job createJob(@RequestBody Job job){

        return jobService.createJob(job);

    }

}
