package com.example.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    private String type;

    private String payload;

    private String status;

    private int retryCount;

}
