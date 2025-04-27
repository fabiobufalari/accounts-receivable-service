// Path: src/main/java/com/bufalari/receivable/entity/ReceivableEntity.java
package com.bufalari.receivable.entity;

import com.bufalari.receivable.auditing.AuditableBaseEntity;
import com.bufalari.receivable.enums.ReceivableStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Import UUID

/**
 * Represents an account receivable (an amount owed by a client).
 * Representa uma conta a receber (um valor devido por um cliente).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "receivables")
public class ReceivableEntity extends AuditableBaseEntity {

    private static final Logger log = LoggerFactory.getLogger(ReceivableEntity.class); // Logger instance

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifier of the client this receivable is due from.
     * Links to the client-service (assuming client IDs are UUIDs).
     * Identificador do cliente de quem esta conta a receber é devida.
     * Liga ao client-service (assumindo que IDs de cliente são UUIDs).
     */
    @NotNull
    @Column(nullable = false)
    private UUID clientId;

    /**
     * Identifier of the project this receivable relates to.
     * Links to the project-management-service.
     * Identificador do projeto ao qual esta conta a receber se refere.
     * Liga ao project-management-service.
     */
    @NotNull
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * Description of the receivable item or service rendered (e.g., "Invoice #INV-123 - Phase 1 Payment").
     * Descrição do item a receber ou serviço prestado (ex: "Fatura #FAT-123 - Pagamento Fase 1").
     */
    @NotNull
    @Column(nullable = false, length = 300)
    private String description;

    /**
     * Reference number for the invoice sent to the client.
     * Número de referência da fatura enviada ao cliente.
     */
    @Column(length = 100, unique = true) // Invoice numbers are often unique
    private String invoiceReference;

    /**
     * Date the receivable (invoice) was issued.
     * Data em que a conta a receber (fatura) foi emitida.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDate issueDate;

    /**
     * Due date for the payment from the client.
     * Data de vencimento do pagamento pelo cliente.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDate dueDate;

    /**
     * Date the payment was actually received. Null if not yet fully received.
     * Data em que o pagamento foi efetivamente recebido. Nulo se ainda não totalmente recebido.
     */
    @Column(nullable = true)
    private LocalDate receivedDate;

    /**
     * The total amount expected to be received.
     * O valor total esperado para recebimento.
     */
    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountExpected;

    /**
     * The amount actually received so far. Can be less than amountExpected for partial payments.
     * O valor efetivamente recebido até o momento. Pode ser menor que amountExpected para pagamentos parciais.
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal amountReceived;

    /**
     * Current status of the receivable (Pending, Received, Overdue, etc.).
     * Status atual da conta a receber (Pendente, Recebido, Atrasado, etc.).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReceivableStatus status;

    /**
     * Reason why the payment is pending, blocked, or in dispute (if applicable).
     * Crucial for financial recovery tracking.
     * Motivo pelo qual o pagamento está pendente, bloqueado ou em disputa (se aplicável).
     * Crucial para rastreamento da recuperação financeira.
     */
    @Column(name = "blocker_reason", length = 1000) // Allow longer text for reasons
    private String blockerReason;

    /**
     * List of references (e.g., IDs or URLs) to supporting documents (invoices, contracts, proof of delivery)
     * stored in the document-storage-service.
     * Lista de referências (ex: IDs ou URLs) para documentos de suporte (faturas, contratos, comprovantes de entrega)
     * armazenados no document-storage-service.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "receivable_document_references", joinColumns = @JoinColumn(name = "receivable_id"))
    @Column(name = "document_reference")
    @Builder.Default
    private List<String> documentReferences = new ArrayList<>();

    // Set default values before persisting
    @PrePersist
    private void setDefaults() {
        if (status == null) {
            status = ReceivableStatus.PENDING;
        }
        if (amountReceived == null) {
            amountReceived = BigDecimal.ZERO;
        }
    }
}