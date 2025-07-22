package org.univ.rankus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class RankusApplication {

    public static void main(String[] args) {
        SpringApplication.run(RankusApplication.class, args);
    }

}
