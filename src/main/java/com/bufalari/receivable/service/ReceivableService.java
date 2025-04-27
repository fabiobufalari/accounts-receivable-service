// Path: src/main/java/com/bufalari/receivable/service/ReceivableService.java
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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for managing Accounts Receivable.
 * Handles CRUD operations and business logic for receivables.
 * Camada de serviço para gerenciamento de Contas a Receber.
 * Trata operações CRUD e lógica de negócio para contas a receber.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReceivableService {

    private static final Logger log = LoggerFactory.getLogger(ReceivableService.class);

    private final ReceivableRepository receivableRepository;
    private final ReceivableConverter receivableConverter;

    /**
     * Creates a new receivable record.
     * Cria um novo registro de conta a receber.
     * @param receivableDTO DTO containing receivable data. / DTO contendo dados da conta a receber.
     * @return The created ReceivableDTO. / O ReceivableDTO criado.
     */
    public ReceivableDTO createReceivable(ReceivableDTO receivableDTO) {
        log.info("Creating new receivable for client ID: {} and project ID: {}", receivableDTO.getClientId(), receivableDTO.getProjectId());
        // Basic validation (could be expanded)
        if (receivableDTO.getClientId() == null || receivableDTO.getProjectId() == null) {
             throw new IllegalArgumentException("Client ID and Project ID are required to create a receivable.");
        }
        ReceivableEntity entity = receivableConverter.dtoToEntity(receivableDTO);
        ReceivableEntity savedEntity = receivableRepository.save(entity);
        log.info("Receivable created successfully with ID: {}", savedEntity.getId());
        return receivableConverter.entityToDTO(savedEntity);
    }

    /**
     * Retrieves a receivable by its unique ID.
     * Recupera uma conta a receber pelo seu ID único.
     * @param id The ID of the receivable. / O ID da conta a receber.
     * @return The found ReceivableDTO. / O ReceivableDTO encontrado.
     * @throws ResourceNotFoundException if not found. / Se não encontrado.
     */
    @Transactional(readOnly = true)
    public ReceivableDTO getReceivableById(Long id) {
        log.debug("Fetching receivable by ID: {}", id);
        return receivableRepository.findById(id)
                .map(receivableConverter::entityToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Receivable not found with ID: " + id));
    }

    /**
     * Retrieves all receivables. Consider using Pageable for large datasets.
     * Recupera todas as contas a receber. Considere usar Pageable para grandes volumes de dados.
     * @return List of all ReceivableDTOs. / Lista de todos os ReceivableDTOs.
     */
    @Transactional(readOnly = true)
    public List<ReceivableDTO> getAllReceivables() {
        log.debug("Fetching all receivables.");
        return receivableRepository.findAll().stream()
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
        // --- CORRECTED IMPLEMENTATION / IMPLEMENTAÇÃO CORRIGIDA ---
        List<ReceivableEntity> entities = receivableRepository.findByStatus(status); // Use repository method
        return entities.stream()
                .map(receivableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

     /**
     * Retrieves overdue receivables (due date is past and not fully paid/canceled/written off).
     * Recupera contas a receber atrasadas (data de vencimento passou e não totalmente pagas/canceladas/baixadas).
     * @return List of overdue ReceivableDTOs. / Lista de ReceivableDTOs atrasados.
     */
    @Transactional(readOnly = true)
    public List<ReceivableDTO> getOverdueReceivables() {
        log.debug("Fetching overdue receivables.");
        LocalDate today = LocalDate.now();
        List<ReceivableStatus> excludedStatuses = List.of(ReceivableStatus.RECEIVED, ReceivableStatus.WRITTEN_OFF, ReceivableStatus.CANCELED);
        List<ReceivableEntity> overdueEntities = receivableRepository.findByDueDateBeforeAndStatusNotIn(today, excludedStatuses);
        return overdueEntities.stream()
                .map(receivableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves receivables that have a non-empty blocker reason set.
     * Recupera contas a receber que têm um motivo de bloqueio não vazio definido.
     */
    @Transactional(readOnly = true)
    public List<ReceivableDTO> getBlockedReceivables() {
        log.debug("Fetching receivables with blockers.");
        List<ReceivableEntity> blockedEntities = receivableRepository.findWithBlockers(); // Use repository method
        return blockedEntities.stream()
                .map(receivableConverter::entityToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Updates an existing receivable record.
     * Atualiza um registro de conta a receber existente.
     * @param id The ID of the receivable to update. / O ID da conta a receber a ser atualizada.
     * @param receivableDTO DTO containing updated data. / DTO contendo dados atualizados.
     * @return The updated ReceivableDTO. / O ReceivableDTO atualizado.
     * @throws ResourceNotFoundException if the receivable is not found. / Se a conta a receber não for encontrada.
     */
    public ReceivableDTO updateReceivable(Long id, ReceivableDTO receivableDTO) {
        log.info("Updating receivable with ID: {}", id);
        ReceivableEntity existingReceivable = receivableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receivable not found with ID: " + id));

        // Basic validation (could be expanded)
        if (receivableDTO.getClientId() == null || receivableDTO.getProjectId() == null) {
             throw new IllegalArgumentException("Client ID and Project ID cannot be null for update.");
        }

        // Update fields from DTO
        existingReceivable.setClientId(receivableDTO.getClientId());
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
        existingReceivable.setDocumentReferences(receivableDTO.getDocumentReferences() != null ? new ArrayList<>(receivableDTO.getDocumentReferences()) : new ArrayList<>());

        ReceivableEntity updatedEntity = receivableRepository.save(existingReceivable);
        log.info("Receivable updated successfully with ID: {}", id);
        return receivableConverter.entityToDTO(updatedEntity);
    }

    /**
     * Partially updates the status, received details, and blocker reason of a receivable.
     * Atualiza parcialmente o status, detalhes de recebimento e motivo de bloqueio de uma conta a receber.
     * @param id The ID of the receivable to update. / O ID da conta a receber a ser atualizada.
     * @param newStatus The new status. / O novo status.
     * @param receivedDate The date the payment was received (if applicable). / A data do recebimento (se aplicável).
     * @param amountReceived The total amount received to date (if applicable). / O valor total recebido até a data (se aplicável).
     * @param blockerReason The reason for a blocker (can be null/empty to clear). / O motivo do bloqueio (pode ser nulo/vazio para limpar).
     * @return The updated ReceivableDTO. / O ReceivableDTO atualizado.
     * @throws ResourceNotFoundException if the receivable is not found. / Se a conta a receber não for encontrada.
     */
    public ReceivableDTO updateReceivableStatus(Long id, ReceivableStatus newStatus, LocalDate receivedDate, BigDecimal amountReceived, String blockerReason) {
        log.info("Updating status/details for receivable ID: {} to Status: {}, ReceivedDate: {}, AmountReceived: {}, Blocker: '{}'",
                 id, newStatus, receivedDate, amountReceived, blockerReason);
        ReceivableEntity receivable = receivableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receivable not found with ID: " + id));

        receivable.setStatus(newStatus);
        if (receivedDate != null) {
            receivable.setReceivedDate(receivedDate);
        }
        if (amountReceived != null) {
             // Assume amountReceived in request sets the *total* amount received
             receivable.setAmountReceived(amountReceived);
             log.debug("Updated amount received for receivable ID {} to {}", id, amountReceived);
        }
        // Allow setting or clearing the blocker reason
        receivable.setBlockerReason(blockerReason);
        log.debug("Updated blocker reason for receivable ID {} to: '{}'", id, blockerReason);


        ReceivableEntity updatedEntity = receivableRepository.save(receivable);
        log.info("Receivable status/details updated successfully for ID: {}", id);
        return receivableConverter.entityToDTO(updatedEntity);
    }

    /**
     * Deletes a receivable record by its ID.
     * Deleta um registro de conta a receber pelo seu ID.
     * @param id The ID of the receivable to delete. / O ID da conta a receber a ser deletada.
     * @throws ResourceNotFoundException if the receivable is not found. / Se a conta a receber não for encontrada.
     */
    public void deleteReceivable(Long id) {
        log.info("Deleting receivable with ID: {}", id);
        if (!receivableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Receivable not found with ID: " + id);
        }
        // Add dependency checks here if needed (e.g., link to contracts)
        // Adicione verificações de dependência aqui se necessário (ex: link para contratos)
        receivableRepository.deleteById(id);
        log.info("Receivable deleted successfully with ID: {}", id);
    }

    // --- Methods for Financial Recovery Focus ---

    /**
     * Calculates the total amount pending reception (Expected - Received), excluding non-collectible statuses.
     * Calcula o valor total pendente de recebimento (Esperado - Recebido), excluindo status não cobráveis.
     * @return Total pending amount. / Valor total pendente.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingAmount() {
        log.debug("Calculating total pending receivable amount.");
        List<ReceivableStatus> activeStatuses = List.of(ReceivableStatus.PENDING, ReceivableStatus.OVERDUE, ReceivableStatus.PARTIALLY_RECEIVED, ReceivableStatus.IN_DISPUTE);
        List<ReceivableEntity> pendingReceivables = receivableRepository.findAll().stream()
                .filter(r -> activeStatuses.contains(r.getStatus()))
                .toList();

        BigDecimal totalPending = pendingReceivables.stream()
                .map(r -> r.getAmountExpected().subtract(r.getAmountReceived() != null ? r.getAmountReceived() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("Total pending receivable amount calculated: {}", totalPending);
        return totalPending;
    }

    /**
     * Calculates the total amount overdue (Expected - Received for overdue items).
     * Calcula o valor total atrasado (Esperado - Recebido para itens atrasados).
     * @return Total overdue amount. / Valor total atrasado.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalOverdueAmount() {
         log.debug("Calculating total overdue receivable amount.");
         List<ReceivableEntity> overdueEntities = getOverdueReceivables().stream() // Reuse DTO list logic
                 .map(receivableConverter::dtoToEntity) // Convert back to entity for calculation
                 .toList();

        BigDecimal totalOverdue = overdueEntities.stream()
                .map(r -> r.getAmountExpected().subtract(r.getAmountReceived() != null ? r.getAmountReceived() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("Total overdue receivable amount calculated: {}", totalOverdue);
        return totalOverdue;
    }

    // --- STUB Methods for Document Management (to be implemented later) ---
    // --- Métodos STUB para Gerenciamento de Documentos (a serem implementados depois) ---

    /**
     * STUB - Adds a document reference to a receivable (future implementation).
     * STUB - Adiciona uma referência de documento a uma conta a receber (implementação futura).
     */
    public String addDocumentReference(Long receivableId, MultipartFile file) {
         log.warn("STUB METHOD: addDocumentReference called for receivable ID {} and file {}", receivableId, file.getOriginalFilename());
         // 1. Find ReceivableEntity or throw ResourceNotFoundException
         ReceivableEntity receivable = receivableRepository.findById(receivableId)
                 .orElseThrow(() -> new ResourceNotFoundException("Receivable not found with ID: " + receivableId));
         // 2. Call DocumentStorageService via Feign Client to upload file
         // String documentIdOrUrl = documentStorageClient.upload(file); // Fictional call
         String documentIdOrUrl = "stubbed_ref_" + file.getOriginalFilename() + "_" + System.nanoTime();
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
     * STUB - Deletes a document reference from a receivable (future implementation).
     * STUB - Deleta uma referência de documento de uma conta a receber (implementação futura).
     */
     public void deleteDocumentReference(Long receivableId, String documentReference) {
          log.warn("STUB METHOD: deleteDocumentReference called for receivable ID {} and reference {}", receivableId, documentReference);
         // 1. Find ReceivableEntity or throw ResourceNotFoundException
         ReceivableEntity receivable = receivableRepository.findById(receivableId)
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
             // documentStorageClient.delete(documentReference); // Fictional call
         } else {
              log.warn("Document reference '{}' not found on receivable {}", documentReference, receivableId);
             // Optionally throw an exception if the reference must exist to be deleted
             // throw new ResourceNotFoundException("Document reference '" + documentReference + "' not found for receivable ID: " + receivableId);
         }
    }

     /**
     * STUB - Gets document references for a receivable.
     * STUB - Obtém referências de documentos para uma conta a receber.
     */
     @Transactional(readOnly = true)
     public List<String> getDocumentReferences(Long receivableId) {
         log.warn("STUB METHOD: getDocumentReferences called for receivable ID {}", receivableId);
         ReceivableEntity receivable = receivableRepository.findById(receivableId)
                 .orElseThrow(() -> new ResourceNotFoundException("Receivable not found with ID: " + receivableId));
          return receivable.getDocumentReferences() != null ? new ArrayList<>(receivable.getDocumentReferences()) : new ArrayList<>();
     }

}