package com.bufalari.receivable.service;

import com.bufalari.receivable.converter.ReceivableConverter;
import com.bufalari.receivable.dto.ReceivableDTO;
import com.bufalari.receivable.entity.ReceivableEntity;
import com.bufalari.receivable.enums.ReceivableStatus;
import com.bufalari.receivable.exception.ResourceNotFoundException;
import com.bufalari.receivable.repository.ReceivableRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID
import java.util.stream.Collectors;

/**
 * Service layer for managing Accounts Receivable (with UUID IDs).
 * Handles CRUD operations and business logic for receivables.
 * Camada de serviço para gerenciamento de Contas a Receber (com IDs UUID).
 * Trata operações CRUD e lógica de negócio para contas a receber.
 */
@Service
@RequiredArgsConstructor // Injects final fields via constructor
@Transactional // Default transaction propagation
public class ReceivableService {

    private static final Logger log = LoggerFactory.getLogger(ReceivableService.class);

    private final ReceivableRepository receivableRepository;
    private final ReceivableConverter receivableConverter;

    /**
     * Creates a new receivable record.
     * Cria um novo registro de conta a receber.
     * @param receivableDTO DTO containing receivable data. ID must be null. / DTO contendo dados da conta a receber. ID deve ser nulo.
     * @return The created ReceivableDTO with its generated UUID. / O ReceivableDTO criado com seu UUID gerado.
     */
    public ReceivableDTO createReceivable(ReceivableDTO receivableDTO) {
        log.info("Creating new receivable for client ID: {} and project ID: {}", receivableDTO.getClientId(), receivableDTO.getProjectId());
        if (receivableDTO.getId() != null) {
            log.warn("Attempted to create a receivable with an existing ID ({}). ID will be ignored.", receivableDTO.getId());
            receivableDTO.setId(null); // Ensure ID is null for creation
        }
        // Basic validation (could be expanded with Client/Project existence check via Feign)
        if (receivableDTO.getClientId() == null || receivableDTO.getProjectId() == null) {
            throw new IllegalArgumentException("Client ID and Project ID are required to create a receivable.");
        }
        ReceivableEntity entity = receivableConverter.dtoToEntity(receivableDTO);
        // Entity's @PrePersist handles default status and amountReceived
        ReceivableEntity savedEntity = receivableRepository.save(entity);
        log.info("Receivable created successfully with ID: {}", savedEntity.getId());
        return receivableConverter.entityToDTO(savedEntity);
    }

    /**
     * Retrieves a receivable by its unique UUID.
     * Recupera uma conta a receber pelo seu UUID único.
     * @param id The UUID of the receivable. / O UUID da conta a receber.
     * @return The found ReceivableDTO. / O ReceivableDTO encontrado.
     * @throws ResourceNotFoundException if not found. / Se não encontrado.
     */
    @Transactional(readOnly = true)
    public ReceivableDTO getReceivableById(UUID id) { // <<<--- UUID
        log.debug("Fetching receivable by ID: {}", id);
        return receivableRepository.findById(id) // <<<--- Use findById with UUID
                .map(receivableConverter::entityToDTO)
                .orElseThrow(() -> {
                    log.warn("Receivable not found with ID: {}", id);
                    return new ResourceNotFoundException("Receivable not found with ID: " + id);
                });
    }

    /**
     * Retrieves all receivables. Consider using Pageable for large datasets.
     * Recupera todas as contas a receber. Considere usar Pageable para grandes volumes de dados.
     * @return List of all ReceivableDTOs. / Lista de todos os ReceivableDTOs.
     */
    @Transactional(readOnly = true)
    public List<ReceivableDTO> getAllReceivables() {
        log.debug("Fetching all receivables.");
        List<ReceivableEntity> entities = receivableRepository.findAll();
        log.info("Retrieved {} receivable entities.", entities.size());
        return entities.stream()
                .map(receivableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves receivables filtered by status.
     * Recupera contas a receber filtradas por status.
     * @param status The status to filter by. / O status para filtrar.
     * @return List of ReceivableDTOs with the specified status. / Lista de ReceivableDTOs com o status especificado.
     */
    @Transactional(readOnly = true)
    public List<ReceivableDTO> getReceivablesByStatus(ReceivableStatus status) {
        log.debug("Fetching receivables by status: {}", status);
        List<ReceivableEntity> entities = receivableRepository.findByStatus(status);
        log.info("Retrieved {} receivable entities with status {}", entities.size(), status);
        return entities.stream()
                .map(receivableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves overdue receivables (due date is past and not fully settled).
     * Recupera contas a receber atrasadas (data de vencimento passou e não totalmente quitadas/canceladas/baixadas).
     * @return List of overdue ReceivableDTOs. / Lista de ReceivableDTOs atrasados.
     */
    @Transactional(readOnly = true)
    public List<ReceivableDTO> getOverdueReceivables() {
        log.debug("Fetching overdue receivables.");
        LocalDate today = LocalDate.now();
        List<ReceivableStatus> settledStatuses = List.of(
                ReceivableStatus.RECEIVED,      // Totalmente recebido
                ReceivableStatus.WRITTEN_OFF,   // Baixado (incobrável)
                ReceivableStatus.CANCELED       // Cancelado
        );
        List<ReceivableEntity> overdueEntities = receivableRepository.findByDueDateBeforeAndStatusNotIn(today, settledStatuses);
        log.info("Retrieved {} overdue receivable entities.", overdueEntities.size());
        return overdueEntities.stream()
                .map(receivableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves receivables that have a non-null and non-empty blocker reason set.
     * Recupera contas a receber que têm um motivo de bloqueio não nulo e não vazio definido.
     */
    @Transactional(readOnly = true)
    public List<ReceivableDTO> getBlockedReceivables() {
        log.debug("Fetching receivables with blockers.");
        List<ReceivableEntity> blockedEntities = receivableRepository.findWithBlockers(); // Use default repository method
        log.info("Retrieved {} receivable entities with blockers.", blockedEntities.size());
        return blockedEntities.stream()
                .map(receivableConverter::entityToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Updates an existing receivable record identified by its UUID.
     * Atualiza um registro de conta a receber existente identificado por seu UUID.
     * @param id The UUID of the receivable to update. / O UUID da conta a receber a ser atualizada.
     * @param receivableDTO DTO containing updated data. ID in DTO is ignored. / DTO contendo dados atualizados. ID no DTO é ignorado.
     * @return The updated ReceivableDTO. / O ReceivableDTO atualizado.
     * @throws ResourceNotFoundException if the receivable is not found. / Se a conta a receber não for encontrada.
     */
    public ReceivableDTO updateReceivable(UUID id, ReceivableDTO receivableDTO) { // <<<--- UUID
        log.info("Updating receivable with ID: {}", id);
        ReceivableEntity existingReceivable = receivableRepository.findById(id) // <<<--- Use findById with UUID
                .orElseThrow(() -> {
                    log.warn("Update failed: Receivable not found with ID: {}", id);
                    return new ResourceNotFoundException("Receivable not found with ID: " + id);
                });

        // Basic validation (could be expanded)
        if (receivableDTO.getClientId() == null || receivableDTO.getProjectId() == null) {
            throw new IllegalArgumentException("Client ID and Project ID cannot be null for update.");
        }

        // Update fields from DTO
        existingReceivable.setClientId(receivableDTO.getClientId()); // <<<--- Update client UUID
        existingReceivable.setProjectId(receivableDTO.getProjectId());
        existingReceivable.setDescription(receivableDTO.getDescription());
        existingReceivable.setInvoiceReference(receivableDTO.getInvoiceReference());
        existingReceivable.setIssueDate(receivableDTO.getIssueDate());
        existingReceivable.setDueDate(receivableDTO.getDueDate());
        existingReceivable.setReceivedDate(receivableDTO.getReceivedDate());
        existingReceivable.setAmountExpected(receivableDTO.getAmountExpected());
        existingReceivable.setAmountReceived(receivableDTO.getAmountReceived() != null ? receivableDTO.getAmountReceived() : BigDecimal.ZERO);
        existingReceivable.setStatus(receivableDTO.getStatus());
        existingReceivable.setBlockerReason(receivableDTO.getBlockerReason());
        // Handle document references update if needed, might require comparison logic
        existingReceivable.setDocumentReferences(receivableDTO.getDocumentReferences() != null ? new ArrayList<>(receivableDTO.getDocumentReferences()) : new ArrayList<>());

        ReceivableEntity updatedEntity = receivableRepository.save(existingReceivable);
        log.info("Receivable updated successfully with ID: {}", id);
        return receivableConverter.entityToDTO(updatedEntity);
    }

    /**
     * Partially updates the status, received details, and blocker reason of a receivable by its UUID.
     * Atualiza parcialmente o status, detalhes de recebimento e motivo de bloqueio de uma conta a receber por seu UUID.
     * @param id The UUID of the receivable to update. / O UUID da conta a receber a ser atualizada.
     * @param newStatus The new status. / O novo status.
     * @param receivedDate The date the payment was received (if applicable). / A data do recebimento (se aplicável).
     * @param amountReceived The total amount received to date (if applicable). / O valor total recebido até a data (se aplicável).
     * @param blockerReason The reason for a blocker (can be null/empty to clear). / O motivo do bloqueio (pode ser nulo/vazio para limpar).
     * @return The updated ReceivableDTO. / O ReceivableDTO atualizado.
     * @throws ResourceNotFoundException if the receivable is not found. / Se a conta a receber não for encontrada.
     */
    public ReceivableDTO updateReceivableStatus(UUID id, ReceivableStatus newStatus, LocalDate receivedDate, BigDecimal amountReceived, String blockerReason) { // <<<--- UUID
        log.info("Updating status/details for receivable ID: {} to Status: {}, ReceivedDate: {}, AmountReceived: {}, Blocker: '{}'",
                id, newStatus, receivedDate, amountReceived, blockerReason);
        ReceivableEntity receivable = receivableRepository.findById(id) // <<<--- Use findById with UUID
                .orElseThrow(() -> {
                    log.warn("Status update failed: Receivable not found with ID: {}", id);
                    return new ResourceNotFoundException("Receivable not found with ID: " + id);
                });

        // Apply updates selectively
        receivable.setStatus(newStatus);

        if (receivedDate != null) {
            receivable.setReceivedDate(receivedDate);
            log.debug("Set receivedDate for receivable ID {} to {}", id, receivedDate);
        }

        if (amountReceived != null) {
            // Typically, this sets the *total* amount received.
            // Add logic here if it represents an *additional* amount received.
            receivable.setAmountReceived(amountReceived);
            log.debug("Set amountReceived for receivable ID {} to {}", id, amountReceived);
        }

        // Allow setting or clearing the blocker reason
        // Use null check to avoid setting null explicitly if not provided, unless clearing is intended
        if (blockerReason != null) { // Check if parameter was provided
            receivable.setBlockerReason(blockerReason.isBlank() ? null : blockerReason); // Set to null if blank, otherwise set value
            log.debug("Updated blockerReason for receivable ID {} to: '{}'", id, receivable.getBlockerReason());
        }

        // Optional: Add logic to automatically update status based on amounts/dates if needed
        // Example: if (receivable.getAmountReceived().compareTo(receivable.getAmountExpected()) >= 0) { receivable.setStatus(ReceivableStatus.RECEIVED); }

        ReceivableEntity updatedEntity = receivableRepository.save(receivable);
        log.info("Receivable status/details updated successfully for ID: {}", id);
        return receivableConverter.entityToDTO(updatedEntity);
    }

    /**
     * Deletes a receivable record by its UUID.
     * Deleta um registro de conta a receber pelo seu UUID.
     * @param id The UUID of the receivable to delete. / O UUID da conta a receber a ser deletada.
     * @throws ResourceNotFoundException if the receivable is not found. / Se a conta a receber não for encontrada.
     */
    public void deleteReceivable(UUID id) { // <<<--- UUID
        log.info("Attempting to delete receivable with ID: {}", id);
        if (!receivableRepository.existsById(id)) { // <<<--- Use existsById with UUID
            log.warn("Delete failed: Receivable not found with ID: {}", id);
            throw new ResourceNotFoundException("Receivable not found with ID: " + id);
        }
        // Add dependency checks here if needed (e.g., link to contracts)
        // Adicione verificações de dependência aqui se necessário (ex: link para contratos)
        receivableRepository.deleteById(id); // <<<--- Use deleteById with UUID
        log.info("Receivable deleted successfully with ID: {}", id);
    }

    // --- Methods for Financial Recovery Focus ---

    /**
     * Calculates the total amount pending reception (Expected - Received), excluding non-collectible/canceled statuses.
     * Calcula o valor total pendente de recebimento (Esperado - Recebido), excluindo status não cobráveis/cancelados.
     * @return Total pending amount. / Valor total pendente.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingAmount() {
        log.debug("Calculating total pending receivable amount.");
        List<ReceivableStatus> activeStatuses = List.of(
                ReceivableStatus.PENDING,
                ReceivableStatus.OVERDUE,
                ReceivableStatus.PARTIALLY_RECEIVED,
                ReceivableStatus.IN_DISPUTE
        );
        // Optimization: Could use a custom repository query to sum in the DB
        List<ReceivableEntity> activeReceivables = receivableRepository.findAll().stream()
                .filter(r -> activeStatuses.contains(r.getStatus()))
                .toList();

        BigDecimal totalPending = activeReceivables.stream()
                // Calculate remaining amount for each: Expected - Received (handle null Received)
                .map(r -> r.getAmountExpected().subtract(r.getAmountReceived() != null ? r.getAmountReceived() : BigDecimal.ZERO))
                // Ensure we don't sum negative balances if overpayments are possible but not desired here
                .filter(balance -> balance.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Total pending receivable amount calculated: {}", totalPending);
        return totalPending;
    }

    /**
     * Calculates the total amount overdue (Remaining amount for overdue items).
     * Calcula o valor total atrasado (Valor restante para itens atrasados).
     * @return Total overdue amount. / Valor total atrasado.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalOverdueAmount() {
        log.debug("Calculating total overdue receivable amount.");
        // Reuse the logic to get overdue entities
        List<ReceivableEntity> overdueEntities = receivableRepository.findByDueDateBeforeAndStatusNotIn(
                LocalDate.now(),
                List.of(ReceivableStatus.RECEIVED, ReceivableStatus.WRITTEN_OFF, ReceivableStatus.CANCELED)
        );

        BigDecimal totalOverdue = overdueEntities.stream()
                .map(r -> r.getAmountExpected().subtract(r.getAmountReceived() != null ? r.getAmountReceived() : BigDecimal.ZERO))
                .filter(balance -> balance.compareTo(BigDecimal.ZERO) > 0) // Only sum positive remaining balances
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Total overdue receivable amount calculated: {}", totalOverdue);
        return totalOverdue;
    }

    // --- STUB Methods for Document Management (Updated Signatures) ---

    /**
     * STUB - Adds a document reference to a receivable by its UUID.
     * STUB - Adiciona uma referência de documento a uma conta a receber por seu UUID.
     */
    public String addDocumentReference(UUID receivableId, MultipartFile file) { // <<<--- UUID
        log.warn("STUB METHOD: addDocumentReference called for receivable ID {} and file {}", receivableId, file.getOriginalFilename());
        // 1. Find ReceivableEntity or throw ResourceNotFoundException
        ReceivableEntity receivable = receivableRepository.findById(receivableId) // <<<--- Use findById with UUID
                .orElseThrow(() -> new ResourceNotFoundException("Receivable not found with ID: " + receivableId));
        // 2. Call DocumentStorageService via Feign Client to upload file (Example)
        // String documentIdOrUrl = documentStorageClient.upload(file);
        String documentIdOrUrl = "stubbed_receivable_ref_" + file.getOriginalFilename() + "_" + System.nanoTime();
        // 3. Add the reference to the entity's list
        if (receivable.getDocumentReferences() == null) {
            receivable.setDocumentReferences(new ArrayList<>());
        }
        receivable.getDocumentReferences().add(documentIdOrUrl);
        // 4. Save the updated entity
        receivableRepository.save(receivable);
        log.info("STUB: Added document reference '{}' to receivable {}", documentIdOrUrl, receivableId);
        return documentIdOrUrl;
    }

    /**
     * STUB - Deletes a document reference from a receivable by its UUID.
     * STUB - Deleta uma referência de documento de uma conta a receber por seu UUID.
     */
    public void deleteDocumentReference(UUID receivableId, String documentReference) { // <<<--- UUID
        log.warn("STUB METHOD: deleteDocumentReference called for receivable ID {} and reference {}", receivableId, documentReference);
        // 1. Find ReceivableEntity or throw ResourceNotFoundException
        ReceivableEntity receivable = receivableRepository.findById(receivableId) // <<<--- Use findById with UUID
                .orElseThrow(() -> new ResourceNotFoundException("Receivable not found with ID: " + receivableId));
        // 2. Remove the reference from the list
        boolean removed = false;
        if (receivable.getDocumentReferences() != null) {
            removed = receivable.getDocumentReferences().remove(documentReference);
        }
        // 3. If removed, save the entity
        if (removed) {
            receivableRepository.save(receivable);
            log.info("STUB: Removed document reference '{}' from receivable {}", documentReference, receivableId);
            // 4. Optionally, call DocumentStorageService via Feign Client to delete the actual file
            // documentStorageClient.delete(documentReference);
        } else {
            log.warn("Document reference '{}' not found on receivable {}", documentReference, receivableId);
            // Optionally throw an exception
            // throw new ResourceNotFoundException("Document reference '" + documentReference + "' not found for receivable ID: " + receivableId);
        }
    }

    /**
     * STUB - Gets document references for a receivable by its UUID.
     * STUB - Obtém referências de documentos para uma conta a receber por seu UUID.
     */
    @Transactional(readOnly = true)
    public List<String> getDocumentReferences(UUID receivableId) { // <<<--- UUID
        log.warn("STUB METHOD: getDocumentReferences called for receivable ID {}", receivableId);
        ReceivableEntity receivable = receivableRepository.findById(receivableId) // <<<--- Use findById with UUID
                .orElseThrow(() -> new ResourceNotFoundException("Receivable not found with ID: " + receivableId));
        return receivable.getDocumentReferences() != null ? new ArrayList<>(receivable.getDocumentReferences()) : new ArrayList<>();
    }

}