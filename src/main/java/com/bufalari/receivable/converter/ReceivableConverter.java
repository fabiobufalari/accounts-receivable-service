// Path: src/main/java/com/bufalari/receivable/converter/ReceivableConverter.java
package com.bufalari.receivable.converter;

import com.bufalari.receivable.dto.ReceivableDTO;
import com.bufalari.receivable.entity.ReceivableEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Converts between ReceivableEntity and ReceivableDTO.
 * Converte entre ReceivableEntity e ReceivableDTO.
 */
@Component
public class ReceivableConverter {

    /**
     * Converts ReceivableEntity to ReceivableDTO.
     * Converte ReceivableEntity para ReceivableDTO.
     */
    public ReceivableDTO entityToDTO(ReceivableEntity entity) {
        if (entity == null) {
            return null;
        }
        return ReceivableDTO.builder()
                .id(entity.getId())
                .clientId(entity.getClientId())
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
     * Converte ReceivableDTO para ReceivableEntity.
     */
    public ReceivableEntity dtoToEntity(ReceivableDTO dto) {
        if (dto == null) {
            return null;
        }
        return ReceivableEntity.builder()
                .id(dto.getId()) // Keep ID for updates
                .clientId(dto.getClientId())
                .projectId(dto.getProjectId())
                .description(dto.getDescription())
                .invoiceReference(dto.getInvoiceReference())
                .issueDate(dto.getIssueDate())
                .dueDate(dto.getDueDate())
                .receivedDate(dto.getReceivedDate())
                .amountExpected(dto.getAmountExpected())
                .amountReceived(dto.getAmountReceived()) // Defaults handled by @PrePersist
                .status(dto.getStatus()) // Defaults handled by @PrePersist
                .blockerReason(dto.getBlockerReason())
                .documentReferences(dto.getDocumentReferences() != null ? new ArrayList<>(dto.getDocumentReferences()) : new ArrayList<>())
                .build();
    }
}