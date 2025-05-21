package org.vomzersocials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableCaching
@ComponentScan(basePackages = "org.vomzersocials",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = org.vomzersocials.user.cors.CorsConfig.class
        ))
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}