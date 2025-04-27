// Path: src/main/java/com/bufalari/receivable/client/AuthServiceClient.java
package com.bufalari.receivable.client; // <<<--- PACOTE CORRETO

import com.bufalari.receivable.dto.UserDetailsDTO; // Import DTO from correct package
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service-client-receivable", url = "${auth.service.url}") // Unique name
public interface AuthServiceClient {

    @GetMapping("/api/users/username/{username}") // VERIFY Endpoint
    UserDetailsDTO getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/api/users/{id}") // VERIFY Endpoint
    UserDetailsDTO getUserById(@PathVariable("id") String userId);
}