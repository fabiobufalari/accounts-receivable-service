package com.bufalari.receivable.auditing;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for entities that require auditing fields.
 * Classe base para entidades que requerem campos de auditoria.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableBaseEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy; // Stores the username or system identifier / Armazena o nome de usuário ou identificador do sistema

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Stores the creation timestamp / Armazena o timestamp de criação

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy; // Stores the username or system identifier of the last modifier / Armazena o nome de usuário ou identificador do sistema do último modificador

    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedAt; // Stores the last modification timestamp / Armazena o timestamp da última modificação
}