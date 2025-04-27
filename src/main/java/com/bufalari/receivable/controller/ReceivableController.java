// Path: src/main/java/com/bufalari/receivable/controller/ReceivableController.java
package com.bufalari.receivable.controller;

import com.bufalari.receivable.dto.ReceivableDTO;
import com.bufalari.receivable.enums.ReceivableStatus;
import com.bufalari.receivable.exception.ResourceNotFoundException;
import com.bufalari.receivable.service.ReceivableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import for security
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing accounts receivable.
 * Controlador REST para gerenciamento de contas a receber.
 */
@RestController
@RequestMapping("/api/receivables")
@RequiredArgsConstructor
@Tag(name = "Accounts Receivable", description = "Endpoints for managing accounts receivable / Endpoints para gerenciamento de contas a receber")
@SecurityRequirement(name = "bearerAuth") // Apply security later
public class ReceivableController {

    private static final Logger log = LoggerFactory.getLogger(ReceivableController.class);
    private final ReceivableService receivableService;

    // --- CREATE ---
    @Operation(summary = "Create Receivable", description = "Creates a new account receivable record.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Receivable created successfully", content = @Content(schema = @Schema(implementation = ReceivableDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        // Add 401/403 when security is implemented
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')") // Example roles
    public ResponseEntity<ReceivableDTO> createReceivable(@Valid @RequestBody ReceivableDTO receivableDTO) {
        log.info("Received request to create receivable: {}", receivableDTO.getDescription());
        ReceivableDTO createdReceivable = receivableService.createReceivable(receivableDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdReceivable.getId())
                .toUri();
        log.info("Receivable created with ID {} at location {}", createdReceivable.getId(), location);
        return ResponseEntity.created(location).body(createdReceivable);
    }

    // --- READ ---
    @Operation(summary = "Get Receivable by ID", description = "Retrieves details of a specific account receivable.")
    @ApiResponses(value = { /* ... */ })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()") // Allow any authenticated user
    public ResponseEntity<ReceivableDTO> getReceivableById(@PathVariable Long id) {
        log.debug("Received request to get receivable by ID: {}", id);
        return ResponseEntity.ok(receivableService.getReceivableById(id));
    }

    @Operation(summary = "Get All Receivables", description = "Retrieves receivables, optionally filtered by status or blocker presence.")
    @ApiResponses(value = { /* ... */ })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReceivableDTO>> getAllReceivables(
            @Parameter(description = "Filter by status") @RequestParam(required = false) ReceivableStatus status,
            @Parameter(description = "Filter for receivables with blockers") @RequestParam(required = false) Boolean hasBlocker) { // New filter
        log.debug("Received request to get receivables, status filter: {}, hasBlocker: {}", status, hasBlocker);
        List<ReceivableDTO> receivables;
        if (Boolean.TRUE.equals(hasBlocker)) { // Check if filtering by blocker
             receivables = receivableService.getBlockedReceivables();
        } else if (status != null) {
            receivables = receivableService.getReceivablesByStatus(status);
        } else {
            receivables = receivableService.getAllReceivables();
        }
        return ResponseEntity.ok(receivables);
    }

     @Operation(summary = "Get Overdue Receivables", description = "Retrieves a list of all overdue accounts receivable.")
     @ApiResponses(value = { /* ... */ })
    @GetMapping(value = "/overdue", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReceivableDTO>> getOverdueReceivables() {
         log.debug("Received request to get overdue receivables");
         return ResponseEntity.ok(receivableService.getOverdueReceivables());
     }

    // --- UPDATE ---
    @Operation(summary = "Update Receivable", description = "Updates an existing account receivable record.")
    @ApiResponses(value = { /* ... */ })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')") // Example roles
    public ResponseEntity<ReceivableDTO> updateReceivable(@PathVariable Long id, @Valid @RequestBody ReceivableDTO receivableDTO) {
        log.info("Received request to update receivable ID: {}", id);
        return ResponseEntity.ok(receivableService.updateReceivable(id, receivableDTO));
    }

    @Operation(summary = "Update Receivable Status/Details", description = "Partially updates status, payment details, and blocker reason.")
    @ApiResponses(value = { /* ... */ })
    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES', 'MANAGER')") // Wider access for status updates maybe?
    public ResponseEntity<ReceivableDTO> updateReceivableStatus(
            @PathVariable Long id,
            @Parameter(description = "New status", required = true) @RequestParam ReceivableStatus status,
            @Parameter(description = "Date payment was received (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate receivedDate,
            @Parameter(description = "Total amount received to date") @RequestParam(required = false) BigDecimal amountReceived,
            @Parameter(description = "Reason for pending/blocker status") @RequestParam(required = false) String blockerReason) {
        log.info("Received request to update status/details for receivable ID {} to {}", id, status);
        return ResponseEntity.ok(receivableService.updateReceivableStatus(id, status, receivedDate, amountReceived, blockerReason));
    }

    // --- DELETE ---
    @Operation(summary = "Delete Receivable", description = "Deletes an account receivable record by ID.")
    @ApiResponses(value = { /* ... */ })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Typically only admins delete financial records
    public ResponseEntity<Void> deleteReceivable(@PathVariable Long id) {
        log.info("Received request to delete receivable ID: {}", id);
        receivableService.deleteReceivable(id);
        return ResponseEntity.noContent().build();
    }

     // --- Aggregate Endpoints ---
    @Operation(summary = "Get Total Pending Amount", description = "Calculates the total amount expected but not fully received.")
    @ApiResponses(value = { /* ... */ })
    @GetMapping(value = "/summary/pending-amount", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalPendingAmount() {
        log.debug("Received request for total pending receivable amount");
        return ResponseEntity.ok(receivableService.getTotalPendingAmount());
    }

    @Operation(summary = "Get Total Overdue Amount", description = "Calculates the total amount overdue.")
    @ApiResponses(value = { /* ... */ })
    @GetMapping(value = "/summary/overdue-amount", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalOverdueAmount() {
         log.debug("Received request for total overdue receivable amount");
        return ResponseEntity.ok(receivableService.getTotalOverdueAmount());
    }


    // --- DOCUMENT MANAGEMENT ENDPOINTS (STUBS) ---
    // Add endpoints similar to PayableController for uploading/getting/deleting document references
    // Adicione endpoints similares ao PayableController para upload/get/delete de referências de documentos
    // Remember to adjust @PreAuthorize roles as needed
    // Lembre-se de ajustar as roles @PreAuthorize conforme necessário

    @PostMapping(value = "/{receivableId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<String> uploadReceivableDocument(@PathVariable Long receivableId, @RequestParam("file") MultipartFile file) {
        log.info("Received request to upload document for receivable ID: {}", receivableId);
         if (file == null || file.isEmpty()) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
        }
        // --- STUB ---
        // TODO: Call service layer
        String documentReference = "receivable-doc-stub-" + System.currentTimeMillis();
        receivableService.getReceivableById(receivableId); // Check existence
        log.warn("Document upload STUB called for receivable {}, file '{}'. Reference: {}", receivableId, file.getOriginalFilename(), documentReference);
        // --- END STUB ---
         try {
             URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/receivables/{receivableId}/documents/{docRef}")
                .buildAndExpand(receivableId, documentReference).toUri();
            return ResponseEntity.created(location).body(documentReference);
        } catch (Exception e) {
             log.error("Failed to build location URI for receivable document ID {}: {}", receivableId, e.getMessage(), e);
             return ResponseEntity.ok(documentReference);
        }
    }

    @GetMapping(value = "/{receivableId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getReceivableDocumentReferences(@PathVariable Long receivableId) {
        log.debug("Received request to get document references for receivable ID: {}", receivableId);
         // --- STUB ---
        ReceivableDTO receivable = receivableService.getReceivableById(receivableId);
        List<String> references = receivable.getDocumentReferences();
        log.warn("Document references STUB returning references for receivable {}", receivableId);
         // --- END STUB ---
        return ResponseEntity.ok(references);
    }

    @DeleteMapping("/{receivableId}/documents/{documentReference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<Void> deleteReceivableDocumentReference(
            @PathVariable Long receivableId,
            @PathVariable String documentReference) {
        log.info("Received request to delete document reference '{}' for receivable ID: {}", documentReference, receivableId);
         // --- STUB ---
        receivableService.getReceivableById(receivableId); // Check existence
        log.warn("Document deletion STUB called for receivable {}, reference {}", receivableId, documentReference);
         // --- END STUB ---
        return ResponseEntity.noContent().build();
    }
}