package com.bufalari.receivable.config;

import com.bufalari.receivable.auditing.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProviderReceivable") // Ref é "auditorProviderReceivable"
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProviderReceivable() { // Nome do bean é "auditorProviderReceivable"
        return new AuditorAwareImpl(); // Localizado em 'auditing' - OK
    }
}