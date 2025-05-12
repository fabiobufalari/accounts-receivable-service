package com.bufalari.receivable.converter;

import com.bufalari.receivable.dto.ReceivableDTO;
import com.bufalari.receivable.entity.ReceivableEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Converts between ReceivableEntity (with UUID ID) and ReceivableDTO (with UUID ID).
 * Converte entre ReceivableEntity (com ID UUID) e ReceivableDTO (com ID UUID).
 */
@Component
public class ReceivableConverter {

    /**
     * Converts ReceivableEntity to ReceivableDTO.
     * Converte ReceivableEntity (com ID UUID) para ReceivableDTO (com ID UUID).
     */
    public ReceivableDTO entityToDTO(ReceivableEntity entity) {
        if (entity == null) {
            return null;
        }
        return ReceivableDTO.builder()
                .id(entity.getId())                 // <<<--- UUID
                .clientId(entity.getClientId())     // <<<--- UUID
                .projectId(entity.getProjectId())
                .description(entity.getDescription())
                .invoiceReference(entity.getInvoiceReference())
                .issueDate(entity.getIssueDate())
                .dueDate(entity.getDueDate())
                .receivedDate(entity.getReceivedDate())
                .amountExpected(entity.getAmountExpected())
                .amountReceived(entity.getAmountReceived())
                .status(entity.getStatus())
                .blockerReason(entity.getBlockerReason())
                .documentReferences(entity.getDocumentReferences() != null ? new ArrayList<>(entity.getDocumentReferences()) : new ArrayList<>())
                .build();
    }

    /**
     * Converts ReceivableDTO to ReceivableEntity.
     * Converte ReceivableDTO (com ID UUID) para ReceivableEntity (com ID UUID).
     */
    public ReceivableEntity dtoToEntity(ReceivableDTO dto) {
        if (dto == null) {
            return null;
        }
        return ReceivableEntity.builder()
                .id(dto.getId())                 // <<<--- UUID (Keep ID for updates)
                .clientId(dto.getClientId())     // <<<--- UUID
                .projectId(dto.getProjectId())
                .description(dto.getDescription())
                .invoiceReference(dto.getInvoiceReference())
                .issueDate(dto.getIssueDate())
                .dueDate(dto.getDueDate())
                .receivedDate(dto.getReceivedDate())
                .amountExpected(dto.getAmountExpected())
                .amountReceived(dto.getAmountReceived()) // Defaults handled by @PrePersist if null
                .status(dto.getStatus()) // Defaults handled by @PrePersist if null
                .blockerReason(dto.getBlockerReason())
                .documentReferences(dto.getDocumentReferences() != null ? new ArrayList<>(dto.getDocumentReferences()) : new ArrayList<>())
                .build();
    }
}