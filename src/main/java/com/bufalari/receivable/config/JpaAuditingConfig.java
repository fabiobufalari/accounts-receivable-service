// Path: src/main/java/com/bufalari/receivable/config/JpaAuditingConfig.java
package com.bufalari.receivable.config;

import com.bufalari.receivable.auditing.AuditorAwareImpl; // Import implementation
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProviderReceivable") // Use unique bean name
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProviderReceivable() { // Bean name matches ref
        return new AuditorAwareImpl();
    }
}