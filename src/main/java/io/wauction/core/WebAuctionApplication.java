package io.wauction.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@EnableReactiveMongoAuditing
@SpringBootApplication
public class WebAuctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAuctionApplication.class, args);
    }

}
