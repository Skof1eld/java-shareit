package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShareItApp {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(15000);
        SpringApplication.run(ShareItApp.class, args);
    }
}
