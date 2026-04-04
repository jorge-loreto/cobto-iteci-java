package com.iteci.cobro.repository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.persistence.EntityManagerFactory;

@Configuration
public class JpaCacheEvictOnStartUp {

    @Bean
    ApplicationRunner evictCacheOnStartup2(ListaAsistenciaRepository listaAsistenciaRepository, EntityManagerFactory emf) {
        return args -> {
     
            listaAsistenciaRepository.findAll(); // This will load all entities into the cache
            if(emf.getCache() != null) {
                emf.getCache().evictAll(); // Clear the second-level cache
            }
     
        };
    }
    
}
