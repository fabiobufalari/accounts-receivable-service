package com.bufalari.receivable.dto;

import com.bufalari.receivable.enums.ReceivableStatus;
import io.swagger.v3.oas.annotations.media.Schema; // Import Schema
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
 * DTO for Account Receivable data transfer. Uses UUID for ID and ClientID.
 * DTO para transferência de dados de Contas a Receber. Usa UUID para ID e ClientID.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceivableDTO {

    @Schema(description = "Unique identifier (UUID) of the receivable", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef", readOnly = true)
    private UUID id; // <<<--- UUID (Read-only in response, set by DB)

    @NotNull(message = "Client ID cannot be null / ID do Cliente não pode ser nulo")
    @Schema(description = "Unique identifier (UUID) of the client this receivable is from", example = "f0e9d8c7-b6a5-4321-fedc-ba9876543210")
    private UUID clientId; // <<<--- UUID (Matches entity)

    @NotNull(message = "Project ID cannot be null / ID do Projeto não pode ser nulo")
    @Schema(description = "ID of the project this receivable relates to", example = "55")
    private Long projectId; // Assuming Project ID remains Long

    @NotBlank(message = "Description cannot be blank / Descrição não pode ser vazia")
    @Size(max = 300)
    @Schema(description = "Description of the receivable item or service", example = "Phase 1 Consulting Services")
    private String description;

    @Size(max = 100)
    @Schema(description = "Invoice reference number sent to the client", example = "INV-2024-05-001", nullable = true)
    private String invoiceReference;

    @NotNull(message = "Issue date cannot be null / Data de emissão não pode ser nula")
    @Schema(description = "Date the receivable/invoice was issued", example = "2024-05-01")
    private LocalDate issueDate;

    @NotNull(message = "Due date cannot be null / Data de vencimento não pode ser nula")
    @Schema(description = "Date payment is due from the client", example = "2024-05-31")
    private LocalDate dueDate;

    @Schema(description = "Date the payment was actually received", example = "2024-06-02", nullable = true)
    private LocalDate receivedDate; // Nullable

    @NotNull(message = "Amount expected cannot be null / Valor esperado não pode ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount expected must be positive / Valor esperado deve ser positivo")
    @Schema(description = "Total amount expected to be received", example = "5000.00")
    private BigDecimal amountExpected;

    @Schema(description = "Amount actually received so far (can be partial)", example = "2500.00", nullable = true)
    private BigDecimal amountReceived; // Nullable, defaults to 0 in entity/service logic

    @NotNull(message = "Status cannot be null / Status não pode ser nulo")
    @Schema(description = "Current status of the receivable", example = "PARTIALLY_RECEIVED")
    private ReceivableStatus status;

    @Size(max = 1000, message = "Blocker reason max length is 1000 / Motivo bloqueio: tamanho máx 1000")
    @Schema(description = "Reason if payment is pending, blocked, or in dispute", example = "Client requested clarification on item X", nullable = true)
    private String blockerReason; // Important for recovery

    @Schema(description = "List of document references (IDs/URLs) associated with this receivable", nullable = true, readOnly = true)
    private List<String> documentReferences;
}