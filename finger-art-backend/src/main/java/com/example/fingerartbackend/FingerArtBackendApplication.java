package com.example.fingerartbackend;

import com.example.fingerartbackend.config.CorsProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
public class FingerArtBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FingerArtBackendApplication.class, args);
    }


    @Bean
    public WebMvcConfigurer corsConfigurer(CorsProperties corsProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = corsProperties.getAllowedOrigins().toArray(String[]::new);
                registry.addMapping("/**").allowedOrigins(origins);
            }
        };
    }
}


