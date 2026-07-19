package com.yasarbilgi.visitormeetingmanagment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class VisitorMeetingManagmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitorMeetingManagmentApplication.class, args);
    }

}
