// Path: src/main/java/com/bufalari/receivable/dto/ReceivableDTO.java
package com.bufalari.receivable.dto;

import com.bufalari.receivable.enums.ReceivableStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID; // Import UUID

/**
 * DTO for Account Receivable data transfer.
 * DTO para transferência de dados de Contas a Receber.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceivableDTO {

    private Long id; // Read-only

    @NotNull(message = "Client ID cannot be null / ID do Cliente não pode ser nulo")
    private UUID clientId; // Matches entity

    @NotNull(message = "Project ID cannot be null / ID do Projeto não pode ser nulo")
    private Long projectId;

    @NotBlank(message = "Description cannot be blank / Descrição não pode ser vazia")
    @Size(max = 300)
    private String description;

    @Size(max = 100)
    private String invoiceReference;

    @NotNull(message = "Issue date cannot be null / Data de emissão não pode ser nula")
    private LocalDate issueDate;

    @NotNull(message = "Due date cannot be null / Data de vencimento não pode ser nula")
    private LocalDate dueDate;

    private LocalDate receivedDate; // Nullable

    @NotNull(message = "Amount expected cannot be null / Valor esperado não pode ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount expected must be positive / Valor esperado deve ser positivo")
    private BigDecimal amountExpected;

    private BigDecimal amountReceived; // Nullable, defaults to 0

    @NotNull(message = "Status cannot be null / Status não pode ser nulo")
    private ReceivableStatus status;

    @Size(max = 1000, message = "Blocker reason max length is 1000 / Motivo bloqueio: tamanho máx 1000")
    private String blockerReason; // Important for recovery

    private List<String> documentReferences;
}