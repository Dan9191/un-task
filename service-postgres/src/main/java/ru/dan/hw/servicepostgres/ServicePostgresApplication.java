package ru.dan.hw.servicepostgres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServicePostgresApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicePostgresApplication.class, args);
    }

}
