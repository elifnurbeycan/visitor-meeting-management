package com.yasarbilgi.visitormeetingmanagment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling
public class VisitorMeetingManagmentApplication {


    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"));

        SpringApplication.run(VisitorMeetingManagmentApplication.class, args);
    }

}
