// Path: src/main/java/com/bufalari/receivable/exception/ResourceNotFoundException.java
package com.bufalari.receivable.exception; // Ensure correct package

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Custom exception thrown when a requested resource (like a Receivable) is not found.
 * Maps to HTTP 404 Not Found status code.
 * Exceção customizada lançada quando um recurso solicitado (como uma Conta a Receber) não é encontrado.
 * Mapeia para o código de status HTTP 404 Not Found.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND) // Indicate the HTTP status to return
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor with a detail message.
     * Construtor com uma mensagem de detalhe.
     * @param message The detail message. / A mensagem de detalhe.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with a detail message and a cause.
     * Construtor com uma mensagem de detalhe e uma causa.
     * @param message The detail message. / A mensagem de detalhe.
     * @param cause The underlying cause. / A causa subjacente.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}