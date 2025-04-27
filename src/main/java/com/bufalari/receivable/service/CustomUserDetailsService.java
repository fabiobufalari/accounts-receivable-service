package com.bufalari.receivable.service;

import com.bufalari.receivable.client.AuthServiceClient;
import com.bufalari.receivable.dto.UserDetailsDTO;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Use Autowired or Constructor Injection
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Service to load user-specific data by calling the Authentication Service.
 * This implementation is used by Spring Security during JWT validation.
 * Serviço para carregar dados específicos do usuário fazendo uma chamada ao Serviço de Autenticação.
 * Esta implementação é usada pelo Spring Security durante a validação do JWT.
 */
@Service("receivableUserDetailsService") // Specific bean name
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final AuthServiceClient authServiceClient;

    // Constructor Injection is preferred / Injeção via Construtor é preferível
    @Autowired // Or use @RequiredArgsConstructor on the class
    public CustomUserDetailsService(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    /**
     * Loads the user's details by username by calling the authentication service.
     * Carrega os detalhes do usuário pelo nome de usuário chamando o serviço de autenticação.
     *
     * @param username The username identifying the user whose data is required. / O nome de usuário que identifica o usuário cujos dados são necessários.
     * @return A fully populated UserDetails object. / Um objeto UserDetails totalmente populado.
     * @throws UsernameNotFoundException if the user could not be found or the auth service is unavailable. / se o usuário não pôde ser encontrado ou o serviço de autenticação está indisponível.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("[Receivable] Attempting to load user details for username: {}", username);
        UserDetailsDTO userDetailsDTO;
        try {
            // Call the Feign client to get user details from the auth service
            // Chama o cliente Feign para obter detalhes do usuário do serviço de autenticação
            userDetailsDTO = authServiceClient.getUserByUsername(username);

            // Check if the DTO itself is null (unexpected from Feign unless endpoint returns null explicitly)
            // Verifica se o próprio DTO é nulo (inesperado do Feign, a menos que o endpoint retorne nulo explicitamente)
            if (userDetailsDTO == null) {
                log.warn("[Receivable] User details DTO received as null for username: {}", username);
                throw new UsernameNotFoundException("User not found (null response from auth service): " + username);
            }

            log.info("[Receivable] Successfully loaded user details via auth service for username: {}", username);
            // Map the received DTO to Spring Security's UserDetails object
            // Mapeia o DTO recebido para o objeto UserDetails do Spring Security
            return new User(
                    userDetailsDTO.getUsername(),
                    // Use empty password as it's not needed for JWT validation here
                    // Usa senha vazia pois não é necessária para validação JWT aqui
                    userDetailsDTO.getPassword() != null ? userDetailsDTO.getPassword() : "",
                    // Map roles to GrantedAuthority objects (prefixing with ROLE_ is standard)
                    // Mapeia roles para objetos GrantedAuthority (prefixar com ROLE_ é padrão)
                    userDetailsDTO.getRoles() != null ?
                            userDetailsDTO.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                    .collect(Collectors.toList())
                            : Collections.emptyList() // Handle null roles list gracefully / Trata lista de roles nula
            );

        } catch (FeignException.NotFound e) {
            // Specific handling for 404 from the auth service
            // Tratamento específico para 404 do serviço de autenticação
            log.warn("[Receivable] User not found via auth service for username: {}. Feign status: 404", username);
            throw new UsernameNotFoundException("User not found: " + username, e);
        } catch (FeignException e) {
            // --- IMPROVED LOGGING FOR OTHER FEIGN ERRORS ---
            // --- LOG MELHORADO PARA OUTROS ERROS FEIGN ---
            log.error("[Receivable] Feign error calling auth service for username: {}. Status: {}, Response: {}",
                    username, e.status(), e.contentUTF8(), e); // Log status and response body
            throw new UsernameNotFoundException("Failed to load user details (auth service communication error status " + e.status() + ") for user: " + username, e);
        } catch (Exception e) {
            // Catch any other unexpected errors during the process
            // Captura quaisquer outros erros inesperados durante o processo
            log.error("[Receivable] Unexpected error loading user details for username: {}", username, e);
            throw new UsernameNotFoundException("Unexpected error loading user details for user: " + username, e);
        }
    }
}