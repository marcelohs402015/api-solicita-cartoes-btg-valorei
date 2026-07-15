package com.btg.proposals;

import com.btg.proposals.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ProposalsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProposalsApiApplication.class, args);
    }
}
