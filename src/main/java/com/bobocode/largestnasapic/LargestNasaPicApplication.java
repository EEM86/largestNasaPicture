package com.bobocode.largestnasapic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class LargestNasaPicApplication {

  public static void main(String[] args) {
    SpringApplication.run(LargestNasaPicApplication.class, args);
  }

}
