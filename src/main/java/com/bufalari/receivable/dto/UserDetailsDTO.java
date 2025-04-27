// Path: src/main/java/com/bufalari/receivable/dto/UserDetailsDTO.java
package com.bufalari.receivable.dto; // <<<--- PACOTE CORRETO

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data // Lombok generates getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDTO {
    private UUID id;
    private String username; // <<--- getUsername() will be generated
    private String password; // <<--- getPassword() will be generated
    private List<String> roles; // <<--- getRoles() will be generated
}