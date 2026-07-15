package com.btg.proposals.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Kafka kafka = new Kafka();
    private Cors cors = new Cors();
    private Security security = new Security();
    private Email email = new Email();

    @Getter
    @Setter
    public static class Kafka {
        private String topic;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins;
    }

    @Getter
    @Setter
    public static class Security {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class Email {
        private String defaultRecipient;
    }
}
