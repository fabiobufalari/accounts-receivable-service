package com.bufalari.receivable.client; // Pacote correto

import com.bufalari.receivable.dto.UserDetailsDTO; // Pacote correto
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service-client-receivable", url = "${auth.service.url}") // Nome único, URL via application.yml
public interface AuthServiceClient {

    // <<< AJUSTE NO PATH >>>
    @GetMapping("/users/username/{username}")
    UserDetailsDTO getUserByUsername(@PathVariable("username") String username);

    // <<< AJUSTE NO PATH >>>
    @GetMapping("/users/{id}")
    UserDetailsDTO getUserById(@PathVariable("id") String userId);
}