package com.codeit.project.sb08deokhugamteamgwanwoong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Sb08DeokhugamTeamGwanwoongApplication {

  public static void main(String[] args) {
    SpringApplication.run(Sb08DeokhugamTeamGwanwoongApplication.class, args);
  }

}
