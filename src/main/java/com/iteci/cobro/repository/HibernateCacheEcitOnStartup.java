package com.iteci.cobro.repository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class HibernateCacheEcitOnStartup {

    @Bean
    ApplicationRunner evictCacheOnStartup(EntityManagerFactory emf) {
        return args -> {
            var sf = emf.unwrap(org.hibernate.SessionFactory.class);
            if(sf !=null && sf.getCache() != null) {
                sf.getCache().evictAll(); // Clear the second-level cache
            }
        };
    }
    
}
