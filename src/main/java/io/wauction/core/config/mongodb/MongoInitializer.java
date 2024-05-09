package io.wauction.core.config.mongodb;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

@Profile("test")
@RequiredArgsConstructor
@Configuration
public class MongoInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        mongoTemplate.dropCollection("auctionOrder");
        mongoTemplate.dropCollection("bids");
    }
}
