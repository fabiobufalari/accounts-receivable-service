package com.bufalari.receivable.controller;

import com.bufalari.receivable.dto.ReceivableDTO;
import com.bufalari.receivable.enums.ReceivableStatus;
import com.bufalari.receivable.exception.ResourceNotFoundException; // Import ResourceNotFoundException
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
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * REST controller for managing accounts receivable.
 * Controlador REST para gerenciamento de contas a receber.
 */
@RestController
@RequestMapping("/api/receivables")
@RequiredArgsConstructor
@Tag(name = "Accounts Receivable", description = "Endpoints for managing accounts receivable / Endpoints para gerenciamento de contas a receber")
@SecurityRequirement(name = "bearerAuth") // Assume security is applied globally
public class ReceivableController {

    private static final Logger log = LoggerFactory.getLogger(ReceivableController.class);
    private final ReceivableService receivableService;

    // --- CREATE ---
    @Operation(summary = "Create Receivable", description = "Creates a new account receivable record.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Receivable created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReceivableDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')")
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
    @Operation(summary = "Get Receivable by ID", description = "Retrieves details of a specific account receivable by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receivable found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReceivableDTO.class))),
            @ApiResponse(responseCode = "404", description = "Receivable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()") // Allow any authenticated user, adjust roles if needed
    public ResponseEntity<ReceivableDTO> getReceivableById(
            @Parameter(description = "UUID of the receivable") @PathVariable UUID id) { // <<<--- UUID
        log.debug("Received request to get receivable by ID: {}", id);
        return ResponseEntity.ok(receivableService.getReceivableById(id));
    }

    @Operation(summary = "Get All Receivables", description = "Retrieves receivables, optionally filtered by status or blocker presence.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receivables retrieved", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReceivableDTO>> getAllReceivables(
            @Parameter(description = "Filter by status") @RequestParam(required = false) ReceivableStatus status,
            @Parameter(description = "Filter for receivables with blockers") @RequestParam(required = false) Boolean hasBlocker) {
        log.debug("Received request to get receivables, status filter: {}, hasBlocker: {}", status, hasBlocker);
        List<ReceivableDTO> receivables;
        if (Boolean.TRUE.equals(hasBlocker)) {
            receivables = receivableService.getBlockedReceivables();
        } else if (status != null) {
            receivables = receivableService.getReceivablesByStatus(status);
        } else {
            receivables = receivableService.getAllReceivables();
        }
        return ResponseEntity.ok(receivables);
    }

    @Operation(summary = "Get Overdue Receivables", description = "Retrieves a list of all overdue accounts receivable.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue receivables retrieved", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/overdue", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReceivableDTO>> getOverdueReceivables() {
        log.debug("Received request to get overdue receivables");
        return ResponseEntity.ok(receivableService.getOverdueReceivables());
    }

    // --- UPDATE ---
    @Operation(summary = "Update Receivable", description = "Updates an existing account receivable record by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receivable updated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReceivableDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Receivable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<ReceivableDTO> updateReceivable(
            @Parameter(description = "UUID of the receivable to update") @PathVariable UUID id, // <<<--- UUID
            @Valid @RequestBody ReceivableDTO receivableDTO) {
        log.info("Received request to update receivable ID: {}", id);
        // Optionally check if ID in path matches ID in body
        if (receivableDTO.getId() != null && !receivableDTO.getId().equals(id)) {
            log.warn("Path ID {} does not match body ID {}. Using path ID.", id, receivableDTO.getId());
            // You might throw a 400 Bad Request here or proceed using the path ID
        }
        return ResponseEntity.ok(receivableService.updateReceivable(id, receivableDTO));
    }

    @Operation(summary = "Update Receivable Status/Details", description = "Partially updates status, payment details, and blocker reason for a receivable by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receivable status updated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReceivableDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status or data"),
            @ApiResponse(responseCode = "404", description = "Receivable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE) // Changed to PATCH, more appropriate for partial updates
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES', 'MANAGER')")
    public ResponseEntity<ReceivableDTO> updateReceivableStatus(
            @Parameter(description = "UUID of the receivable") @PathVariable UUID id, // <<<--- UUID
            @Parameter(description = "New status", required = true) @RequestParam ReceivableStatus status,
            @Parameter(description = "Date payment was received (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate receivedDate,
            @Parameter(description = "Total amount received to date") @RequestParam(required = false) BigDecimal amountReceived,
            @Parameter(description = "Reason for pending/blocker status") @RequestParam(required = false) String blockerReason) {
        log.info("Received request to update status/details for receivable ID {} to {}", id, status);
        return ResponseEntity.ok(receivableService.updateReceivableStatus(id, status, receivedDate, amountReceived, blockerReason));
    }

    // --- DELETE ---
    @Operation(summary = "Delete Receivable", description = "Deletes an account receivable record by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Receivable deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Receivable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReceivable(
            @Parameter(description = "UUID of the receivable to delete") @PathVariable UUID id) { // <<<--- UUID
        log.info("Received request to delete receivable ID: {}", id);
        receivableService.deleteReceivable(id);
        return ResponseEntity.noContent().build();
    }

    // --- Aggregate Endpoints ---
    @Operation(summary = "Get Total Pending Amount", description = "Calculates the total amount expected but not fully received across active receivables.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total pending amount calculated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/summary/pending-amount", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalPendingAmount() {
        log.debug("Received request for total pending receivable amount");
        return ResponseEntity.ok(receivableService.getTotalPendingAmount());
    }

    @Operation(summary = "Get Total Overdue Amount", description = "Calculates the total amount overdue across all receivables.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total overdue amount calculated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/summary/overdue-amount", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalOverdueAmount() {
        log.debug("Received request for total overdue receivable amount");
        return ResponseEntity.ok(receivableService.getTotalOverdueAmount());
    }


    // --- DOCUMENT MANAGEMENT ENDPOINTS (STUBS) ---
    // Remember to adjust @PreAuthorize roles as needed

    @Operation(summary = "Upload Document for Receivable (STUB)", description = "Attaches a document to a specific receivable by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document reference created", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or empty file"),
            @ApiResponse(responseCode = "404", description = "Receivable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping(value = "/{receivableId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<String> uploadReceivableDocument(
            @Parameter(description = "UUID of the receivable") @PathVariable UUID receivableId, // <<<--- UUID
            @Parameter(description = "Document file to upload") @RequestParam("file") MultipartFile file) {
        log.info("Received request to upload document for receivable ID: {}", receivableId);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
        }
        // --- STUB ---
        // TODO: Call service layer to handle actual upload and reference persistence
        String documentReference = receivableService.addDocumentReference(receivableId, file); // Call the stub/real service method
        log.warn("[STUB] Document upload called for receivable {}, file '{}'. Generated Reference: {}", receivableId, file.getOriginalFilename(), documentReference);
        // --- END STUB ---
        try {
            URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/receivables/{receivableId}/documents/{docRef}") // Assuming future GET for doc ref
                    .buildAndExpand(receivableId, documentReference).toUri();
            return ResponseEntity.created(location).body(documentReference);
        } catch (Exception e) {
            log.error("Failed to build location URI for receivable document ID {}: {}", receivableId, e.getMessage(), e);
            // Fallback if URI creation fails
            return ResponseEntity.ok(documentReference);
        }
    }

    @Operation(summary = "Get Document References for Receivable (STUB)", description = "Retrieves a list of document references for a specific receivable by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document references retrieved", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "Receivable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{receivableId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getReceivableDocumentReferences(
            @Parameter(description = "UUID of the receivable") @PathVariable UUID receivableId) { // <<<--- UUID
        log.debug("Received request to get document references for receivable ID: {}", receivableId);
        // --- STUB ---
        // TODO: Call service layer
        List<String> references = receivableService.getDocumentReferences(receivableId); // Call the stub/real service method
        log.warn("[STUB] Document references returning for receivable {}: {}", receivableId, references);
        // --- END STUB ---
        return ResponseEntity.ok(references);
    }

    @Operation(summary = "Delete Document Reference for Receivable (STUB)", description = "Deletes a specific document reference associated with a receivable by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document reference deleted"),
            @ApiResponse(responseCode = "404", description = "Receivable or document reference not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{receivableId}/documents/{documentReference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<Void> deleteReceivableDocumentReference(
            @Parameter(description = "UUID of the receivable") @PathVariable UUID receivableId, // <<<--- UUID
            @Parameter(description = "The document reference string to delete") @PathVariable String documentReference) {
        log.info("Received request to delete document reference '{}' for receivable ID: {}", documentReference, receivableId);
        // --- STUB ---
        // TODO: Call service layer
        receivableService.deleteDocumentReference(receivableId, documentReference); // Call the stub/real service method
        log.warn("[STUB] Document deletion called for receivable {}, reference {}", receivableId, documentReference);
        // --- END STUB ---
        return ResponseEntity.noContent().build();
    }
}