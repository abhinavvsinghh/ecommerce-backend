package com.nagarro.amcart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Component
@Slf4j
public class ProfileConfig {

    @Autowired
    private Environment environment;

    @PostConstruct
    public void logApplicationStartup() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            log.warn("No active profiles set, running with default configuration");
        } else {
            log.info("Running with active profile(s): {}", Arrays.toString(profiles));
        }
        
        // Log additional environment information for cloud deployment
        if (Arrays.asList(profiles).contains("cloud")) {
            log.info("Application is running in CLOUD environment");
            log.info("Client origin: {}", environment.getProperty("client.origin"));
            log.info("MongoDB database: {}", environment.getProperty("spring.data.mongodb.database"));
            log.info("ElasticSearch host: {}", environment.getProperty("elasticsearch.host"));
        } else if (Arrays.asList(profiles).contains("local")) {
            log.info("Application is running in LOCAL environment");
        }
    }
}