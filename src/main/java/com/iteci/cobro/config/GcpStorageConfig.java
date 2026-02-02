package com.iteci.cobro.config;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GcpStorageConfig {

    @Bean
    public Storage storage() throws IOException {
        // Load JSON from resources
        InputStream serviceAccount = getClass()
                .getClassLoader()
                .getResourceAsStream("iteci-c8bd4-400c9da51782.json");

        if (serviceAccount == null) {
            throw new RuntimeException("GCS service account JSON not found in resources!");
        }

        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
                .getService();
    }
}
