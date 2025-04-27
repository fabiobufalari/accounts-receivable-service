// Path: src/main/java/com/bufalari/receivable/auditing/AuditorAwareImpl.java
package com.bufalari.receivable.auditing;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
// import org.springframework.stereotype.Component; // REMOVA @Component

import java.util.Optional;

// NO @Component annotation here / SEM anotação @Component aqui
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // <<<--- ADJUST HERE / AJUSTE AQUI ---<<<
            return Optional.of("system_receivable"); // System user specific to this service
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof User) {
           username = ((User) principal).getUsername();
        } else if (principal instanceof String) {
           username = (String) principal;
        } else {
             return Optional.of("unknown_user");
        }
        return Optional.of(username);
    }
}